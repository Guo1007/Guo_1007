package com.example.furnituresystem.service;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    public void sendVerifyCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("家具商城 - 验证码");
            message.setText("您的验证码是：" + code + "，有效期1分钟，请勿泄露给他人。");
            mailSender.send(message);
            log.info("验证码邮件已发送至 {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败: {} -> {}", to, e.getMessage());
        }
    }

    @Async
    public void sendNotificationEmail(String to, String title, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("家具商城 - " + title);
            message.setText(content + "\n\n--- 家具商城通知 ---");
            mailSender.send(message);
            log.info("通知邮件已发送至 {}", to);
        } catch (Exception e) {
            log.error("通知邮件发送失败: {} -> {}", to, e.getMessage());
        }
    }

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
}
