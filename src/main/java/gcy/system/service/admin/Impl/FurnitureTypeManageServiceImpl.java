package gcy.system.service.admin.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.dto.admin.AdminFurnitureTypeFormDTO;
import gcy.system.entity.pojo.Furniture;
import gcy.system.entity.pojo.FurnitureType;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.FurnitureMapper;
import gcy.system.mapper.FurnitureTypeMapper;
import gcy.system.service.admin.IFurnitureTypeManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FurnitureTypeManageServiceImpl extends ServiceImpl<FurnitureTypeMapper, FurnitureType> implements IFurnitureTypeManageService {

    private final FurnitureMapper furnitureMapper;


    @Override
    @Transactional
    public Result addFurnitureType(AdminFurnitureTypeFormDTO dto) {
        FurnitureType type = BeanUtil.toBean(dto, FurnitureType.class);
        boolean success = this.save(type);
        return success ? Result.okMsg("添加成功") : Result.fail("添加失败");
    }

    @Override
    @Transactional
    public Result editFurnitureType(AdminFurnitureTypeFormDTO dto) {
        if (dto.getId() == null) {
            return Result.fail("ID 不能为空");
        }
        FurnitureType type = BeanUtil.toBean(dto, FurnitureType.class);
        boolean success = this.updateById(type);
        return success ? Result.okMsg("更新成功") : Result.fail("更新失败");
    }

    @Override
    @Transactional
    public Result deleteFurnitureType(Long id) {
        // 检查是否有家具关联此分类
        Long furnitureCount = furnitureMapper.selectCount(
                new LambdaQueryWrapper<Furniture>().eq(Furniture::getTypeId, id));
        if (furnitureCount > 0) {
            throw new BusinessException("该分类下有 " + furnitureCount + " 件商品，请先迁移或删除商品后再操作");
        }
        boolean success = this.removeById(id);
        return success ? Result.okMsg("删除成功") : Result.fail("删除失败");
    }

    @Override
    public Result getFurnitureTypeById(Long id) {
        FurnitureType type = this.getById(id);
        if (type == null) {
            return Result.fail("数据不存在");
        }
        AdminFurnitureTypeFormDTO dto = BeanUtil.toBean(type, AdminFurnitureTypeFormDTO.class);
        return Result.ok(dto);
    }

    @Override
    public Result getFurnitureTypeList(Integer current, Integer size, String typeName) {
        Page<FurnitureType> page = new Page<>(current, size);
        LambdaQueryWrapper<FurnitureType> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(typeName)) {
            queryWrapper.like(FurnitureType::getName, typeName);
        }
        Page<FurnitureType> resultPage = page(page, queryWrapper);
        if (resultPage.getTotal() == 0) {
            return Result.fail("暂未发现任何数据！");
        }
        return Result.ok(resultPage);
    }
}