package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.OperationLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper 接口。
 *
 * @author 郭名城
 * @date 2026-08-14
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogPO> {
}