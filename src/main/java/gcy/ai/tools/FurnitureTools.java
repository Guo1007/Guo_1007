package gcy.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.agent.tool.Tool;
import gcy.system.entity.pojo.Furniture;
import gcy.system.entity.pojo.FurnitureType;
import gcy.system.entity.pojo.Sku;
import gcy.system.entity.pojo.SkuSpec;
import gcy.system.entity.pojo.SpecGroup;
import gcy.system.entity.pojo.SpecValue;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.mapper.FurnitureTypeMapper;
import gcy.system.mapper.SkuMapper;
import gcy.system.mapper.SkuSpecMapper;
import gcy.system.mapper.SpecGroupMapper;
import gcy.system.mapper.SpecValueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FurnitureTools {

    private final FurnitureMapper furnitureMapper;

    private final FurnitureTypeMapper furnitureTypeMapper;

    private final SkuMapper skuMapper;

    private final SkuSpecMapper skuSpecMapper;

    private final SpecGroupMapper specGroupMapper;

    private final SpecValueMapper specValueMapper;

    @Tool("查询所有在售商品的完整列表，包含名称、价格、库存、品牌、分类等信息")
    public String queryAllFurniture() {
        log.debug("调用queryAllFurniture");
        List<Furniture> list = furnitureMapper.selectList(null);
        if (list.isEmpty()) {
            return "目前没有在售商品";
        }
        StringBuilder sb = new StringBuilder("【在售商品列表】\n");
        for (Furniture f : list) {
            String typeName = getTypeName(f.getTypeId());
            sb.append(String.format("- %s | 价格: ¥%s | 库存: %d件 | 品牌: %s | 分类: %s\n",
                    f.getFName(), f.getPrice(), f.getStock(), f.getBrand(), typeName));
        }
        return sb.toString();
    }

    @Tool("根据商品名称模糊搜索商品")
    public String searchFurniture(String name) {
        log.debug("调用searchFurniture");
        List<Furniture> list = furnitureMapper.selectList(
                new LambdaQueryWrapper<Furniture>().like(Furniture::getFName, name));
        if (list.isEmpty()) {
            return "未找到名称中包含「" + name + "」的商品";
        }
        StringBuilder sb = new StringBuilder("【搜索结果】\n");
        for (Furniture f : list) {
            sb.append(String.format("ID:%d | %s | ¥%s | 库存:%d\n简介: %s\n",
                    f.getId(), f.getFName(), f.getPrice(), f.getStock(), f.getIntro()));
        }
        return sb.toString();
    }

    @Tool("查询指定商品的所有SKU规格及每个SKU的库存、价格信息。需要传入商品名称")
    public String querySkuInfo(String furnitureName) {
        log.debug("调用querySkuInfo, furnitureName={}", furnitureName);

        List<Furniture> furnitureList = furnitureMapper.selectList(
                new LambdaQueryWrapper<Furniture>().like(Furniture::getFName, furnitureName));
        if (furnitureList.isEmpty()) {
            return "未找到名称中包含「" + furnitureName + "」的商品";
        }
        if (furnitureList.size() > 1) {
            String names = furnitureList.stream()
                    .map(Furniture::getFName)
                    .collect(Collectors.joining("、"));
            return "找到多个匹配的商品，请指定具体名称：\n" + names;
        }
        Furniture furniture = furnitureList.get(0);
        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getFurnitureId, furniture.getId()));
        if (skus.isEmpty()) {
            return "该商品没有多规格SKU，使用统一价格和库存";
        }
        List<Long> skuIds = skus.stream().map(Sku::getId).collect(Collectors.toList());
        List<SkuSpec> skuSpecs = skuSpecMapper.selectList(
                new LambdaQueryWrapper<SkuSpec>().in(SkuSpec::getSkuId, skuIds));
        StringBuilder sb = new StringBuilder("【").append(furniture.getFName()).append(" 规格库存信息】\n");
        if (skuSpecs.isEmpty()) {
            for (Sku sku : skus) {
                sb.append(String.format("SKU编码: %s | 价格: ¥%s | 库存: %d件\n",
                        sku.getSkuCode(), sku.getPrice(), sku.getStock()));
            }
            return sb.toString();
        }
        Map<Long, List<SkuSpec>> specMap = skuSpecs.stream()
                .collect(Collectors.groupingBy(SkuSpec::getSkuId));
        List<Long> groupIds = skuSpecs.stream().map(SkuSpec::getSpecGroupId).distinct().collect(Collectors.toList());
        List<Long> valueIds = skuSpecs.stream().map(SkuSpec::getSpecValueId).distinct().collect(Collectors.toList());
        Map<Long, String> groupNames = specGroupMapper.selectByIds(groupIds).stream()
                .collect(Collectors.toMap(SpecGroup::getId, SpecGroup::getGroupName));
        Map<Long, String> valueNames = specValueMapper.selectByIds(valueIds).stream()
                .collect(Collectors.toMap(SpecValue::getId, SpecValue::getValueName));
        for (Sku sku : skus) {
            sb.append(String.format("SKU编码: %s | 价格: ¥%s | 库存: %d件",
                    sku.getSkuCode(), sku.getPrice(), sku.getStock()));
            List<SkuSpec> specs = specMap.get(sku.getId());
            if (specs != null && !specs.isEmpty()) {
                sb.append(" | 规格: ");
                for (int i = 0; i < specs.size(); i++) {
                    SkuSpec ss = specs.get(i);
                    String gn = groupNames.getOrDefault(ss.getSpecGroupId(), "");
                    String vn = valueNames.getOrDefault(ss.getSpecValueId(), "");
                    if (i > 0) sb.append(", ");
                    sb.append(gn).append(":").append(vn);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Tool("查询所有商品的总库存概况，包含每个商品的名称、总库存量和SKU数量")
    public String queryStockSummary() {
        log.debug("调用queryStockSummary");
        List<Furniture> furnitureList = furnitureMapper.selectList(null);
        if (furnitureList.isEmpty()) {
            return "暂无商品数据";
        }
        StringBuilder sb = new StringBuilder("【库存概况】\n");
        int totalStock = 0;
        for (Furniture f : furnitureList) {
            int skuCount = skuMapper.selectCount(
                    new LambdaQueryWrapper<Sku>().eq(Sku::getFurnitureId, f.getId())).intValue();
            sb.append(String.format("- %s: 库存%d件%s\n",
                    f.getFName(), f.getStock(), skuCount > 0 ? " (" + skuCount + "个SKU)" : ""));
            totalStock += f.getStock();
        }
        sb.append("\n总库存: ").append(totalStock).append("件");
        return sb.toString();
    }

    private String getTypeName(Long typeId) {
        if (typeId == null) return "未分类";
        FurnitureType type = furnitureTypeMapper.selectById(typeId);
        return type != null ? type.getName() : "未分类";
    }
}
