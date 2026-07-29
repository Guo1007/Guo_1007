package gcy.system.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Furniture;
import gcy.system.entity.vo.TopFurnitureVO;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.mapper.OrderItemMapper;
import gcy.system.service.IFurnitureService;
import gcy.system.utils.RedisData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static gcy.system.utils.RedisConstants.*;

/**
 * 家具服务实现类。
 * <p>
 * 提供家具信息的查询、搜索、排序、缓存管理等功能。
 * 使用Redis缓存提高查询性能，采用逻辑过期+互斥锁策略处理缓存击穿和缓存穿透问题，
 * 通过Redisson分布式锁保证缓存重建时的数据一致性。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FurnitureServiceImpl extends ServiceImpl<FurnitureMapper, Furniture> implements IFurnitureService {

    private final StringRedisTemplate stringRedisTemplate;

    private final FurnitureMapper furnitureMapper;

    private final OrderItemMapper orderItemMapper;

    private final RedissonClient redissonClient;

    /**
     * 根据家具ID查询家具详情。
     * <p>
     * 采用三级缓存策略：首先从Redis缓存中查询；
     * 若缓存命中且未过期则直接返回；若缓存命中但已逻辑过期，则获取分布式锁后异步重建缓存，同时返回旧数据；
     * 若缓存完全缺失，则加互斥锁后查询数据库，查询结果写入Redis缓存，
     * 若数据库中不存在则写入空值标记以防止缓存穿透。
     * 在获取锁后执行Double-Check，避免重复查库。
     * </p>
     *
     * @param id 家具ID
     * @return 包含家具信息的Result对象；若家具不存在则返回404错误信息
     */
    @Override
    public Result queryFurnitureById(Long id) {
        String key = CACHE_FURNITURE_KEY + id;
        String furnitureJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(furnitureJson)) {
            RedisData redisData = JSONUtil.toBean(furnitureJson, RedisData.class);
            Furniture furniture = JSONUtil.toBean((JSONObject) redisData.getData(), Furniture.class);
            if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
                return Result.ok(furniture);
            }
            String lockKey = LOCK_FURNITURE_KEY + id;
            RLock lock = redissonClient.getLock(lockKey);
            boolean tryLock = false;
            try {
                tryLock = lock.tryLock(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取锁被中断, id={}", id);
            }
            if (tryLock) {
                rebuildCacheAsync(id, lock);
            }
            return Result.ok(furniture);
        }
        if (furnitureJson != null) {
            return Result.fail(404, "该家具不存在，请刷新页面后重新选择！");
        }
        // 缓存完全缺失，加锁防止大量并发请求同时穿透到DB
        String lockKey = LOCK_FURNITURE_KEY + id;
        RLock lock = redissonClient.getLock(lockKey);
        boolean tryLock = false;
        try {
            tryLock = lock.tryLock(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取冷缓存锁被中断, id={}", id);
        }
        if (tryLock) {
            try {
                // Double-check: 可能其他线程已重建缓存
                String doubleCheck = stringRedisTemplate.opsForValue().get(key);
                if (StrUtil.isNotBlank(doubleCheck)) {
                    RedisData redisData = JSONUtil.toBean(doubleCheck, RedisData.class);
                    return Result.ok(JSONUtil.toBean((JSONObject) redisData.getData(), Furniture.class));
                }
                if (doubleCheck != null) {
                    return Result.fail(404, "该家具不存在，请刷新页面后重新选择！");
                }
                Furniture furniture = getById(id);
                if (furniture == null) {
                    stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                    return Result.fail(404, "该家具不存在，请刷新页面后重新选择！");
                }
                saveFurniture2Redis(id, CACHE_FURNITURE_TTL);
                return Result.ok(furniture);
            } finally {
                lock.unlock();
            }
        }
        Furniture furniture = getById(id);
        if (furniture == null) {
            return Result.fail(404, "该家具不存在，请刷新页面后重新选择！");
        }
        return Result.ok(furniture);
    }

    /**
     * 根据家具类型ID分页查询家具列表，支持多条件筛选和排序。
     * <p>
     * 支持按家具名称模糊搜索、按品牌精确匹配、按库存状态筛选、按是否推荐筛选，
     * 以及按价格（升/降序）、销量（降序）、最新（近三天创建时间降序）排序。
     * 默认排序为"default"时不应用任何排序规则。
     * </p>
     *
     * @param typeId        家具类型ID，为null或小于等于0时不筛选类型
     * @param current       当前页码
     * @param size          每页条数
     * @param fName         家具名称关键词，支持模糊搜索，为null或空时不筛选
     * @param stockStatus   库存状态：in_stock有库存、low_stock低库存、out_stock无库存，为null或空时不筛选
     * @param brand         品牌名称，为null或空时不筛选
     * @param sortBy        排序字段：price价格、sales销量、newest最新、default默认不排序
     * @param sortOrder     排序方向：asc升序、desc降序（仅price字段使用此参数）
     * @param isRecommended 是否推荐：1为推荐，其他值或不传则不筛选
     * @return 包含分页家具列表的Result对象
     */
    @Override
    public Result getFurnitureByType(Long typeId, Integer current, Integer size,
                                     String fName, String stockStatus, String brand,
                                     String sortBy, String sortOrder,
                                     Integer isRecommended) {
        Page<Furniture> page = new Page<>(current, size);
        LambdaQueryWrapper<Furniture> wrapper = new LambdaQueryWrapper<>();
        if (typeId != null && typeId > 0) {
            wrapper.eq(Furniture::getTypeId, typeId);
        }
        if (StrUtil.isNotBlank(fName)) {
            wrapper.like(Furniture::getFName, fName);
        }
        if (StrUtil.isNotBlank(brand)) {
            wrapper.eq(Furniture::getBrand, brand);
        }
        if (isRecommended != null && isRecommended == 1) {
            wrapper.eq(Furniture::getIsRecommended, 1);
        }
        applyStockStatusFilter(wrapper, stockStatus);
        applySorting(wrapper, sortBy, sortOrder);
        return Result.ok(furnitureMapper.selectPage(page, wrapper));
    }

    /**
     * 查询销量最高的前N件家具（热销榜）。
     * <p>
     * 通过OrderItemMapper查询订单明细表中销量最高的家具记录，返回包含家具名称和销售数量的VO列表。
     * </p>
     *
     * @param limit 返回的家具数量上限
     * @return 包含TopFurnitureVO列表的Result对象，列表中每项包含家具名称和销量
     */
    @Override
    public Result getTopSelling(Integer limit) {
        List<TopFurnitureVO> list = orderItemMapper.selectTopSelling(limit);
        return Result.ok(list);
    }

    /**
     * 根据排序字段和排序方向对MyBatis-Plus查询条件构造器应用排序规则。
     * <p>
     * 排序字段为空或为"default"时不应用任何排序。
     * 支持的排序方式：价格升降序、销量降序、最新（近三天创建时间降序）。
     * </p>
     *
     * @param wrapper   MyBatis-Plus查询条件构造器
     * @param sortBy    排序字段：price按价格、sales按销量、newest按最新
     * @param sortOrder 排序方向：asc升序、desc降序，仅对price字段生效
     */
    private void applySorting(LambdaQueryWrapper<Furniture> wrapper, String sortBy, String sortOrder) {
        if (StrUtil.isBlank(sortBy) || "default".equals(sortBy)) {
            return;
        }
        boolean asc = "asc".equals(sortOrder);
        switch (sortBy) {
            case "price":
                if (asc) {
                    wrapper.orderByAsc(Furniture::getPrice);
                } else {
                    wrapper.orderByDesc(Furniture::getPrice);
                }
                break;
            case "sales":
                wrapper.orderByDesc(Furniture::getSaleCount);
                break;
            case "newest":
                wrapper.ge(Furniture::getCreateTime, LocalDateTime.now().minusDays(3));
                wrapper.orderByDesc(Furniture::getCreateTime);
                break;
            default:
                break;
        }
    }

    /**
     * 根据库存状态对MyBatis-Plus查询条件构造器应用库存筛选。
     * <p>
     * 支持的库存状态：
     * in_stock（有库存，库存量大于0）、
     * low_stock（低库存，库存量大于0且小于10）、
     * out_stock（无库存，库存量等于0）。
     * 其他值或为空时不应用任何筛选。
     * </p>
     *
     * @param wrapper     MyBatis-Plus查询条件构造器
     * @param stockStatus 库存状态字符串：in_stock、low_stock、out_stock
     */
    public static void applyStockStatusFilter(LambdaQueryWrapper<Furniture> wrapper, String stockStatus) {
        if ("in_stock".equals(stockStatus)) {
            wrapper.gt(Furniture::getStock, 0);
        } else if ("low_stock".equals(stockStatus)) {
            wrapper.gt(Furniture::getStock, 0);
            wrapper.lt(Furniture::getStock, 10);
        } else if ("out_stock".equals(stockStatus)) {
            wrapper.eq(Furniture::getStock, 0);
        }
    }

    /**
     * 根据家具类型ID查询该类型下所有可用的品牌列表。
     * <p>
     * 通过FurnitureMapper执行去重查询，获取指定类型下所有出现过品牌名称的列表。
     * </p>
     *
     * @param id 家具类型ID
     * @return 包含品牌名称列表的Result对象；若未查询到任何品牌则返回失败信息
     */
    @Override
    public Result getFurnitureBrandsByTypeId(Long id) {
        List<String> brands = furnitureMapper.getFurnitureBrandsByTypeId(id);
        if (brands == null || brands.isEmpty()) {
            return Result.fail("暂未查询到家具品牌！");
        }
        return Result.ok(brands);
    }

    /**
     * 将家具数据保存到Redis缓存中，并设置逻辑过期时间。
     * <p>
     * 先从数据库查询家具信息，封装为RedisData对象（包含数据和逻辑过期时间）后写入Redis。
     * 若数据库中不存在该家具，则写入空字符串作为空值标记，防止缓存穿透。
     * </p>
     *
     * @param id            家具ID
     * @param expireSeconds 缓存逻辑过期时长（秒）
     */
    private void saveFurniture2Redis(Long id, long expireSeconds) {
        Furniture furniture = getById(id);
        if (furniture == null) {
            stringRedisTemplate.opsForValue().set(CACHE_FURNITURE_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return;
        }
        RedisData redisData = new RedisData();
        redisData.setData(furniture);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_FURNITURE_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 异步重建指定家具的Redis缓存数据。
     * <p>
     * 该方法在缓存逻辑过期后被调用，重新从数据库加载家具数据并写入Redis。
     * 重建完成后释放当前线程持有的分布式锁。
     * 若重建过程中发生异常，记录错误日志但不向外抛出，确保锁能被正确释放。
     * </p>
     *
     * @param id   需要重建缓存的家具ID
     * @param lock 当前线程持有的Redisson分布式锁，方法执行完毕后释放
     */
    public void rebuildCacheAsync(Long id, RLock lock) {
        try {
            log.info("开始重建缓存, id={}", id);
            saveFurniture2Redis(id, 3600);
        } catch (Exception e) {
            log.error("重建缓存失败, id={}", id, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
