package gcy.system.utils;

import cn.hutool.core.util.StrUtil;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.pojo.User;
import gcy.system.integration.EmailService;
import gcy.system.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单状态邮件发送工具类。
 * <p>
 * 统一封装"查询订单所属用户并发送订单状态邮件"的逻辑，
 * 供用户端订单服务与管理端订单管理服务复用，避免两处重复维护、修改遗漏。
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-11
 */
@Slf4j
public final class OrderEmailUtil {

    private OrderEmailUtil() {
    }

    /**
     * 发送订单状态通知邮件。
     * 根据订单所属用户查询其邮箱，存在且非空时发送；发送失败仅记录日志，不影响主流程。
     *
     * @param emailService 邮件发送服务
     * @param userMapper   用户 Mapper，用于查询订单所属用户
     * @param order        订单对象，包含用户ID、订单ID、总金额
     * @param title        邮件标题
     * @param content      邮件正文内容
     * @param statusIcon   订单状态图标（emoji）
     * @param refundRemark 退款原因/处理备注（非退款场景传 null）
     */
    public static void sendOrderStatus(EmailService emailService, UserMapper userMapper,
                                       Order order, String title, String content,
                                       String statusIcon, String refundRemark) {
        try {
            User user = userMapper.selectById(order.getUserId());
            if (user != null && StrUtil.isNotBlank(user.getEmail())) {
                emailService.sendOrderStatusEmail(user.getEmail(), order.getId(), title, content,
                        statusIcon, order.getTotalPrice().toString(), user.getUserName(),
                        refundRemark);
            }
        } catch (Exception e) {
            log.error("发送订单状态邮件失败: orderId={}", order.getId(), e);
        }
    }
}
