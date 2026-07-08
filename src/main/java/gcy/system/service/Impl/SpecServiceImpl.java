package gcy.system.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.FurnitureSpecDTO;
import gcy.system.entity.pojo.*;
import gcy.system.entity.vo.FurnitureSpecVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.*;
import gcy.system.service.ISpecService;
import gcy.system.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecServiceImpl implements ISpecService {

    private final SpecGroupMapper specGroupMapper;

    private final SpecValueMapper specValueMapper;

    private final SkuMapper skuMapper;

    private final SkuSpecMapper skuSpecMapper;

    private final FurnitureMapper furnitureMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getSpecAndSkuByFurnitureId(Long furnitureId) {
        return buildSpecVO(furnitureId, false);
    }

    @Override
    public Result getAvailableSpecAndSku(Long furnitureId) {
        return buildSpecVO(furnitureId, true);
    }

    private Result buildSpecVO(Long furnitureId, boolean onlyAvailable) {
        // 查规格组
        List<SpecGroup> groups = specGroupMapper.selectList(
                new LambdaQueryWrapper<SpecGroup>()
                        .eq(SpecGroup::getFurnitureId, furnitureId)
                        .orderByAsc(SpecGroup::getSort));

        if (groups.isEmpty()) {
            List<Sku> skus = skuMapper.selectList(
                    new LambdaQueryWrapper<Sku>()
                            .eq(Sku::getFurnitureId, furnitureId)
                            .eq(onlyAvailable, Sku::getStatus, 1)
                            .gt(onlyAvailable, Sku::getStock, 0));
            List<FurnitureSpecVO.SkuVO> skuVOs = new ArrayList<>();
            for (Sku s : skus) {
                FurnitureSpecVO.SkuVO skuVO = new FurnitureSpecVO.SkuVO();
                skuVO.setId(s.getId());
                skuVO.setSkuCode(s.getSkuCode());
                skuVO.setPrice(s.getPrice());
                skuVO.setStock(s.getStock());
                skuVO.setSkuImage(s.getSkuImage());
                skuVO.setStatus(s.getStatus());
                skuVO.setSpecMap(Collections.emptyMap());
                skuVO.setSpecText("");
                skuVOs.add(skuVO);
            }
            FurnitureSpecVO vo = new FurnitureSpecVO();
            vo.setSpecGroups(Collections.emptyList());
            vo.setSkuList(skuVOs);
            return Result.ok(vo);
        }

        List<Long> groupIds = groups.stream().map(SpecGroup::getId).collect(Collectors.toList());
        List<SpecValue> allValues = specValueMapper.selectList(
                new LambdaQueryWrapper<SpecValue>()
                        .in(SpecValue::getSpecGroupId, groupIds)
                        .orderByAsc(SpecValue::getSort));
        Map<Long, List<SpecValue>> valuesByGroup = allValues.stream()
                .collect(Collectors.groupingBy(SpecValue::getSpecGroupId));

        List<FurnitureSpecVO.SpecGroupVO> groupVOs = new ArrayList<>();
        for (SpecGroup g : groups) {
            FurnitureSpecVO.SpecGroupVO gvo = new FurnitureSpecVO.SpecGroupVO();
            gvo.setId(g.getId());
            gvo.setGroupName(g.getGroupName());
            gvo.setSort(g.getSort());
            List<SpecValue> vals = valuesByGroup.getOrDefault(g.getId(), Collections.emptyList());
            List<FurnitureSpecVO.SpecValueVO> valueVOs = new ArrayList<>();
            for (SpecValue v : vals) {
                FurnitureSpecVO.SpecValueVO vvo = new FurnitureSpecVO.SpecValueVO();
                vvo.setId(v.getId());
                vvo.setValueName(v.getValueName());
                vvo.setValueImage(v.getValueImage());
                vvo.setSort(v.getSort());
                valueVOs.add(vvo);
            }
            gvo.setValues(valueVOs);
            groupVOs.add(gvo);
        }

        LambdaQueryWrapper<Sku> skuWrapper = new LambdaQueryWrapper<Sku>()
                .eq(Sku::getFurnitureId, furnitureId);
        if (onlyAvailable) {
            skuWrapper.eq(Sku::getStatus, 1).gt(Sku::getStock, 0);
        }
        List<Sku> skus = skuMapper.selectList(skuWrapper);

        List<Long> skuIds = skus.stream().map(Sku::getId).collect(Collectors.toList());
        Map<Long, List<SkuSpec>> skuSpecMap = new HashMap<>();
        Map<Long, Map<String, String>> skuSpecTextMap = new HashMap<>();
        if (!skuIds.isEmpty()) {
            List<SkuSpec> allSkuSpecs = skuSpecMapper.selectList(
                    new LambdaQueryWrapper<SkuSpec>().in(SkuSpec::getSkuId, skuIds));
            skuSpecMap = allSkuSpecs.stream().collect(Collectors.groupingBy(SkuSpec::getSkuId));

            Map<Long, String> groupNameMap = groups.stream()
                    .collect(Collectors.toMap(SpecGroup::getId, SpecGroup::getGroupName));
            Map<Long, String> valueNameMap = allValues.stream()
                    .collect(Collectors.toMap(SpecValue::getId, SpecValue::getValueName));

            for (Map.Entry<Long, List<SkuSpec>> entry : skuSpecMap.entrySet()) {
                Long skuId = entry.getKey();
                Map<String, String> specMap = new LinkedHashMap<>();
                for (SkuSpec ss : entry.getValue()) {
                    String gName = groupNameMap.get(ss.getSpecGroupId());
                    String vName = valueNameMap.get(ss.getSpecValueId());
                    if (gName != null && vName != null) {
                        specMap.put(gName, vName);
                    }
                }
                skuSpecTextMap.put(skuId, specMap);
            }
        }

        List<FurnitureSpecVO.SkuVO> skuVOs = new ArrayList<>();
        for (Sku s : skus) {
            FurnitureSpecVO.SkuVO svo = new FurnitureSpecVO.SkuVO();
            svo.setId(s.getId());
            svo.setSkuCode(s.getSkuCode());
            svo.setPrice(s.getPrice());
            svo.setStock(s.getStock());
            svo.setSkuImage(s.getSkuImage());
            svo.setStatus(s.getStatus());
            Map<String, String> specMap = skuSpecTextMap.getOrDefault(s.getId(), Collections.emptyMap());
            svo.setSpecMap(specMap);
            if (!specMap.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> e : specMap.entrySet()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(e.getKey()).append(":").append(e.getValue());
                }
                svo.setSpecText(sb.toString());
            } else {
                svo.setSpecText("");
            }
            skuVOs.add(svo);
        }

        FurnitureSpecVO vo = new FurnitureSpecVO();
        vo.setSpecGroups(groupVOs);
        vo.setSkuList(skuVOs);
        return Result.ok(vo);
    }

    @Override
    @Transactional
    public Result saveSpecAndSku(FurnitureSpecDTO dto) {
        Long furnitureId = dto.getFurnitureId();
        if (furnitureId == null) {
            return Result.fail("商品ID不能为空");
        }
        Furniture furniture = furnitureMapper.selectById(furnitureId);
        if (furniture == null) {
            return Result.fail("商品不存在");
        }

        List<Sku> oldSkus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getFurnitureId, furnitureId));
        List<Long> oldSkuIds = oldSkus.stream().map(Sku::getId).collect(Collectors.toList());
        if (!oldSkuIds.isEmpty()) {
            skuSpecMapper.delete(
                    new LambdaQueryWrapper<SkuSpec>().in(SkuSpec::getSkuId, oldSkuIds));
            skuMapper.delete(
                    new LambdaQueryWrapper<Sku>().eq(Sku::getFurnitureId, furnitureId));
        }
        List<SpecGroup> oldGroups = specGroupMapper.selectList(
                new LambdaQueryWrapper<SpecGroup>().eq(SpecGroup::getFurnitureId, furnitureId));
        List<Long> oldGroupIds = oldGroups.stream().map(SpecGroup::getId).collect(Collectors.toList());
        if (!oldGroupIds.isEmpty()) {
            specValueMapper.delete(
                    new LambdaQueryWrapper<SpecValue>().in(SpecValue::getSpecGroupId, oldGroupIds));
        }
        specGroupMapper.delete(
                new LambdaQueryWrapper<SpecGroup>().eq(SpecGroup::getFurnitureId, furnitureId));

        List<FurnitureSpecDTO.SpecGroupDTO> groups = dto.getSpecGroups();
        List<FurnitureSpecDTO.SkuDTO> skuDTOs = dto.getSkuList();

        if (groups == null || groups.isEmpty() || skuDTOs == null || skuDTOs.isEmpty()) {
            Sku defaultSku = new Sku();
            defaultSku.setFurnitureId(furnitureId);
            defaultSku.setSkuCode("SKU-" + furnitureId);
            defaultSku.setPrice(furniture.getPrice());
            defaultSku.setStock(furniture.getStock() != null ? furniture.getStock() : 0);
            defaultSku.setStatus(1);
            defaultSku.setCreateTime(LocalDateTime.now());
            skuMapper.insert(defaultSku);
            return Result.ok();
        }

        Map<Long, Long> valueIdMap = new HashMap<>();
        for (FurnitureSpecDTO.SpecGroupDTO groupDTO : groups) {
            SpecGroup group = new SpecGroup();
            group.setFurnitureId(furnitureId);
            group.setGroupName(groupDTO.getGroupName());
            group.setSort(groupDTO.getSort() != null ? groupDTO.getSort() : 0);
            group.setCreateTime(LocalDateTime.now());
            specGroupMapper.insert(group);
            Long newGroupId = group.getId();

            if (groupDTO.getValues() != null) {
                for (FurnitureSpecDTO.SpecValueDTO valueDTO : groupDTO.getValues()) {
                    SpecValue value = new SpecValue();
                    value.setSpecGroupId(newGroupId);
                    value.setValueName(valueDTO.getValueName());
                    value.setValueImage(valueDTO.getValueImage());
                    value.setSort(valueDTO.getSort() != null ? valueDTO.getSort() : 0);
                    specValueMapper.insert(value);
                    if (valueDTO.getId() != null) {
                        valueIdMap.put(valueDTO.getId(), value.getId());
                    }
                }
            }
        }

        // SKU code 唯一性校验
        Set<String> skuCodesInBatch = new HashSet<>();
        for (FurnitureSpecDTO.SkuDTO skuDTO : skuDTOs) {
            String code = StrUtil.isNotBlank(skuDTO.getSkuCode()) ? skuDTO.getSkuCode().trim() : null;
            if (StrUtil.isBlank(code)) {
                throw new BusinessException("SKU编码不能为空，请填写所有SKU编码");
            }
            if (!skuCodesInBatch.add(code)) {
                throw new BusinessException("SKU编码 [" + code + "] 重复，请使用唯一的编码");
            }
        }
        // 检查数据库中是否已有相同编码
        for (FurnitureSpecDTO.SkuDTO skuDTO : skuDTOs) {
            String code = skuDTO.getSkuCode().trim();
            if (skuMapper.selectCount(
                    new LambdaQueryWrapper<Sku>().eq(Sku::getSkuCode, code)) > 0) {
                throw new BusinessException("SKU编码 [" + code + "] 已被其他商品使用，请更换");
            }
        }

        for (FurnitureSpecDTO.SkuDTO skuDTO : skuDTOs) {
            Sku sku = new Sku();
            sku.setFurnitureId(furnitureId);
            sku.setSkuCode(skuDTO.getSkuCode().trim());
            sku.setPrice(skuDTO.getPrice());
            sku.setStock(skuDTO.getStock() != null ? skuDTO.getStock() : 0);
            sku.setSkuImage(skuDTO.getSkuImage());
            sku.setStatus(skuDTO.getStatus() != null ? skuDTO.getStatus() : 1);
            sku.setCreateTime(LocalDateTime.now());
            skuMapper.insert(sku);
            Long newSkuId = sku.getId();

            if (skuDTO.getSpecValueIds() != null) {
                for (Long tempValueId : skuDTO.getSpecValueIds()) {
                    Long realValueId = valueIdMap.getOrDefault(tempValueId, tempValueId);
                    SpecValue sv = specValueMapper.selectById(realValueId);
                    if (sv == null) continue;
                    SkuSpec skuSpec = new SkuSpec();
                    skuSpec.setSkuId(newSkuId);
                    skuSpec.setSpecGroupId(sv.getSpecGroupId());
                    skuSpec.setSpecValueId(realValueId);
                    skuSpecMapper.insert(skuSpec);
                }
            }
        }

        refreshFurniturePriceAndStock(furnitureId);

        return Result.ok();
    }


    public void refreshFurniturePriceAndStock(Long furnitureId) {
        BigDecimal minPrice = skuMapper.minPriceByFurnitureId(furnitureId);
        int totalStock = skuMapper.sumStockByFurnitureId(furnitureId);

        LambdaUpdateWrapper<Furniture> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Furniture::getId, furnitureId);
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) > 0) {
            wrapper.set(Furniture::getPrice, minPrice);
        }
        wrapper.set(Furniture::getStock, totalStock);
        furnitureMapper.update(null, wrapper);

        stringRedisTemplate.delete(RedisConstants.CACHE_FURNITURE_KEY + furnitureId);
    }
}