package gcy.system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 发送HTML邮件（通用方法）
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("家具商城 - " + subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("HTML邮件已发送至 {}", to);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败: {} -> {}", to, e.getMessage());
        }
    }

    /**
     * 发送验证码邮件
     */
    @Async
    public void sendVerifyCode(String to, String code, String action, Long expireMinutes) {
        Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("action", action);
        context.setVariable("expireMinutes", expireMinutes);
        context.setVariable("sendTime", LocalDateTime.now().format(FORMATTER));

        String html = templateEngine.process("email/verify-code", context);
        sendHtmlEmail(to, "验证码", html);
    }

    /**
     * 发送订单状态通知邮件
     */
    @Async
    public void sendOrderStatusEmail(String to, Long orderId, String title, String content,
                                     String statusIcon, String statusColor,
                                     String totalPrice, String userName) {
        Context context = new Context();
        context.setVariable("orderId", orderId);
        context.setVariable("title", title);
        context.setVariable("content", content);
        context.setVariable("statusIcon", statusIcon);
        context.setVariable("statusColor", statusColor);
        context.setVariable("totalPrice", totalPrice);
        context.setVariable("userName", userName);
        context.setVariable("sendTime", LocalDateTime.now().format(FORMATTER));

        String html = templateEngine.process("email/order-status", context);
        sendHtmlEmail(to, title, html);
    }

    /**
     * 发送系统通知邮件
     */
    @Async
    public void sendNotificationEmail(String to, String title, String content) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("content", content);
        context.setVariable("sendTime", LocalDateTime.now().format(FORMATTER));

        String html = templateEngine.process("email/notification", context);
        sendHtmlEmail(to, title, html);
    }

    /**
     * 发送库存预警邮件
     */
    @Async
    public void sendStockAlertEmail(String to, String title, Object items) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("items", items);
        context.setVariable("sendTime", LocalDateTime.now().format(FORMATTER));

        String html = templateEngine.process("email/stock-alert", context);
        sendHtmlEmail(to, title, html);
    }
}
