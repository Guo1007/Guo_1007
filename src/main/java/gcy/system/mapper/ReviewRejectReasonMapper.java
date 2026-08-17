package gcy.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import gcy.system.entity.pojo.ReviewRejectReason;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核拒绝原因模板 Mapper 接口。
 *
 * @author 郭名城
 * @date 2026-08-17
 */
@Mapper
public interface ReviewRejectReasonMapper extends BaseMapper<ReviewRejectReason> {
}