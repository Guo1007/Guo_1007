package gcy.system.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.pojo.OperationLogPO;
import gcy.system.mapper.OperationLogMapper;
import gcy.system.service.IOperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现类。
 *
 * @author 郭名城
 * @date 2026-08-14
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLogPO> implements IOperationLogService {
}