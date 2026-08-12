package gcy.system.service.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import gcy.system.entity.dto.StockAlertItem;
import gcy.system.entity.pojo.AdminNotifySetting;
import gcy.system.entity.pojo.User;
import gcy.system.integration.EmailService;
import gcy.system.mapper.AdminNotifySettingMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.service.admin.Impl.NotifySettingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员通知服务。
 * <p>
 * 统一封装「按通知类型读取后台配置 → 校验开关 → 解析接收管理员 → 发送邮件」的流程，
 * 供有需要的业务模块（新订单、退款申请、库存预警等）复用，避免各处在业务代码中重复实现。
 * 发送失败仅记录日志，不影响主业务流程。
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotifyService {

    private final AdminNotifySettingMapper adminNotifySettingMapper;

    private final UserMapper userMapper;

    private final EmailService emailService;

    /**
     * 根据通知类型解析该类型下应接收通知的管理员邮箱列表。
     * <p>
     * 仅返回已开启通知、且为管理员（isAdmin=1）并已绑定邮箱的收件人。
     * 返回 null 表示配置缺失或开关未开启，调用方应直接跳过；返回空列表表示暂无有效收件人。
     * </p>
     *
     * @param notifyType 通知类型（见 {@link NotifySettingServiceImpl} 中的 TYPE_* 常量）
     * @return 有效管理员邮箱列表；配置缺失或未开启时返回 null
     */
    private List<String> resolveAdminEmails(String notifyType) {
        AdminNotifySetting setting = adminNotifySettingMapper.selectOne(
                new LambdaQueryWrapper<AdminNotifySetting>()
                        .eq(AdminNotifySetting::getNotifyType, notifyType));
        if (setting == null) {
            log.warn("未找到通知配置（notifyType={}），可能未执行 admin_notify_setting 迁移，跳过", notifyType);
            return null;
        }
        if (setting.getEnabled() == null || setting.getEnabled() != 1) {
            log.debug("该功能通知未开启，跳过: {}", notifyType);
            return null;
        }
        List<Long> adminIds = NotifySettingServiceImpl.parseIds(setting.getAdminIds());
        if (adminIds.isEmpty()) {
            log.debug("未配置接收管理员，跳过: {}", notifyType);
            return null;
        }
        List<User> admins = userMapper.selectByIds(adminIds);
        if (admins == null || admins.isEmpty()) {
            return List.of();
        }
        return admins.stream()
                .filter(a -> a.getIsAdmin() != null && a.getIsAdmin() == 1 && StrUtil.isNotBlank(a.getEmail()))
                .map(User::getEmail)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 发送普通管理员通知邮件（如新订单、退款申请）。
     * <p>
     * 受后台对应通知类型的开关与接收人配置控制；开关未开启或配置缺失时静默跳过。
     * </p>
     *
     * @param notifyType 通知类型
     * @param subject    邮件主题
     * @param content    邮件正文
     */
    public void sendNotification(String notifyType, String subject, String content) {
        try {
            List<String> emails = resolveAdminEmails(notifyType);
            if (emails == null || emails.isEmpty()) {
                return;
            }
            emailService.sendNotificationBatch(emails, subject, content);
            log.info("管理员通知已发送: {}, 收件人 {} 位", subject, emails.size());
        } catch (Exception e) {
            log.error("发送管理员通知失败: {}", subject, e);
        }
    }

    /**
     * 发送库存预警邮件，受后台「库存预警」通知配置控制。
     *
     * @param displayItems 邮件内展示的库存不足商品列表（已截取前 15 条）
     * @param totalCount   库存不足商品总数
     */
    public void sendStockAlert(List<StockAlertItem> displayItems, int totalCount) {
        try {
            List<String> emails = resolveAdminEmails(NotifySettingServiceImpl.TYPE_STOCK_ALERT);
            if (emails == null || emails.isEmpty()) {
                return;
            }
            for (String email : emails) {
                emailService.sendStockAlertEmail(email, "库存预警", displayItems, totalCount);
            }
            log.info("库存预警邮件已发送, 收件人 {} 位", emails.size());
        } catch (Exception e) {
            log.error("发送库存预警邮件失败", e);
        }
    }
}