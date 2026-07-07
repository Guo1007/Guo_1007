package gcy.system.service.admin.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureFormDTO;
import gcy.system.entity.pojo.Furniture;
import gcy.system.entity.pojo.OrderItem;
import gcy.system.entity.pojo.Sku;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.OrderItemMapper;
import gcy.system.mapper.SkuMapper;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.service.Impl.FurnitureServiceImpl;
import gcy.system.service.admin.IFurnitureManageService;
import gcy.system.utils.OrderStatus;
import gcy.system.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FurnitureManageServiceImpl extends ServiceImpl<FurnitureMapper, Furniture>
        implements IFurnitureManageService {

    private final FurnitureMapper furnitureMapper;

    private final SkuMapper skuMapper;

    private final OrderItemMapper orderItemMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getFurnitureList(Integer current, Integer size, Long typeId, String fName,
                                   String stockStatus, String brand) {
        Page<Furniture> page = new Page<>(current, size);
        LambdaQueryWrapper<Furniture> wrapper = new LambdaQueryWrapper<>();
        if (typeId != null) {
            wrapper.eq(Furniture::getTypeId, typeId);
        }
        if (StrUtil.isNotBlank(fName)) {
            wrapper.like(Furniture::getFName, fName);
        }
        if (StrUtil.isNotBlank(brand)) {
            wrapper.eq(Furniture::getBrand, brand);
        }
        FurnitureServiceImpl.applyStockStatusFilter(wrapper, stockStatus);
        Page<Furniture> result = furnitureMapper.selectPage(page, wrapper);
        return Result.ok(result);
    }

    @Override
    @Transactional
    public Result addFurniture(AdminFurnitureFormDTO dto) {
        if (dto == null) {
            return Result.fail("请输入完整的新增家具信息！");
        }
        Furniture furniture = BeanUtil.copyProperties(dto, Furniture.class);
        boolean success = save(furniture);
        if (!success) {
            throw new BusinessException("添加家具失败，请联系系统管理人员！");
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result editFurniture(AdminFurnitureFormDTO dto) {
        if (dto == null || dto.getId() == null) {
            return Result.fail("请求参数错误");
        }
        if (furnitureMapper.selectCount(
                new LambdaQueryWrapper<Furniture>().eq(Furniture::getId, dto.getId())) == 0) {
            return Result.fail("家具不存在，无法修改");
        }
        LambdaUpdateWrapper<Furniture> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Furniture::getId, dto.getId());
        wrapper.set(Furniture::getFName, dto.getFName());
        wrapper.set(Furniture::getFIcon, dto.getFIcon());
        wrapper.set(Furniture::getTypeId, dto.getTypeId());
        wrapper.set(Furniture::getBrand, dto.getBrand());

        Long skuCount = skuMapper.selectCount(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getFurnitureId, dto.getId()));
        if (skuCount > 0) {
            int totalStock = skuMapper.sumStockByFurnitureId(dto.getId());
            BigDecimal minPrice = skuMapper.minPriceByFurnitureId(dto.getId());
            wrapper.set(Furniture::getStock, totalStock);
            if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) > 0) {
                wrapper.set(Furniture::getPrice, minPrice);
            }
        } else {
            wrapper.set(Furniture::getStock, dto.getStock());
            wrapper.set(Furniture::getPrice, dto.getPrice());
        }

        if (StrUtil.isNotBlank(dto.getIntro())) {
            wrapper.set(Furniture::getIntro, dto.getIntro());
        }
        if (dto.getImages() != null) {
            wrapper.set(Furniture::getImages, dto.getImages());
        }
        if (dto.getDescription() != null) {
            wrapper.set(Furniture::getDescription, dto.getDescription());
        }
        boolean success = furnitureMapper.update(null, wrapper) > 0;
        if (success) {
            stringRedisTemplate.delete(RedisConstants.CACHE_FURNITURE_KEY + dto.getId());
            return Result.ok("修改成功");
        } else {
            throw new BusinessException("修改失败，请系统联系管理人员！");
        }
    }

    @Override
    @Transactional
    public Result deleteFurniture(Long furnitureId) {
        // 检查是否有未完成的订单关联该家具（通过子查询）
        List<Integer> activeStatuses = Arrays.asList(
                OrderStatus.PENDING_PAYMENT.getCode(),
                OrderStatus.PAID.getCode(),
                OrderStatus.SHIPPED.getCode()
        );
        Long orderItemCount = orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getFurnitureId, furnitureId)
                        .inSql(OrderItem::getOrderId,
                                "SELECT id FROM `order` WHERE status IN (" +
                                        String.join(",", activeStatuses.stream().map(String::valueOf).toList()) + ")")
        );
        if (orderItemCount > 0) {
            throw new BusinessException("该家具存在未完成的订单，无法删除");
        }

        int rows = furnitureMapper.deleteById(furnitureId);
        if (rows > 0) {
            stringRedisTemplate.delete(RedisConstants.CACHE_FURNITURE_KEY + furnitureId);
            return Result.ok();
        }
        return Result.fail("删除失败！");
    }

}
