package gcy.system.aspect;

import gcy.system.entity.dto.UserDTO;
import gcy.system.entity.dto.Result;
import gcy.system.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.StringJoiner;

/**
 * 操作日志切面，自动拦截带有 {@link OperationLog} 注解的方法并记录操作日志。
 * 日志内容包括：操作人、操作描述、关键参数、执行结果。
 *
 * @author 郭名城
 * @date 2026-08-03
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    /**
     * 环绕通知，拦截带有 {@link OperationLog} 注解的方法。
     *
     * @param pjp 切点连接点，封装了被拦截方法的信息
     * @param opLog 操作日志注解，提供操作描述
     * @return 被拦截方法的原始返回值
     * @throws Throwable 被拦截方法可能抛出的异常
     */
    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OperationLog opLog) throws Throwable {
        UserDTO user = UserHolder.getUser();
        String userName = user != null ? user.getUserName() : "匿名";
        String userId = user != null ? String.valueOf(user.getId()) : "-";

        // 提取关键参数（过滤掉 HttpServletRequest/Response、MultipartFile 等非业务参数）
        String params = extractParams(pjp);

        log.info("【操作日志】用户ID:{} | 用户名:{} | 操作:{} | 参数:{}", userId, userName, opLog.value(), params);

        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long cost = System.currentTimeMillis() - start;

        String resultStatus = "失败";
        String resultMsg = "";
        if (result instanceof Result r) {
            resultStatus = Boolean.TRUE.equals(r.getSuccess()) ? "成功" : "失败";
            resultMsg = r.getMsg() != null ? r.getMsg() : "";
        }

        log.info("【操作日志】用户ID:{} | 用户名:{} | 操作:{} | 耗时:{}ms | 结果:{} | 提示:{}",
                userId, userName, opLog.value(), cost, resultStatus, resultMsg);
        return result;
    }

    /**
     * 提取方法的关键业务参数，过滤掉框架对象和文件对象，并对密码类字段脱敏。
     */
    private String extractParams(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return "无";
        }

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] paramNames = signature.getParameterNames();
        StringJoiner sj = new StringJoiner(", ");

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) continue;
            // 过滤非业务参数
            if (args[i] instanceof HttpServletRequest
                    || args[i] instanceof HttpServletResponse
                    || args[i] instanceof MultipartFile) {
                continue;
            }
            String name = paramNames != null && i < paramNames.length ? paramNames[i] : "arg" + i;
            // 参数名为密码类：整个值脱敏
            if (isSensitiveParamName(name)) {
                sj.add(name + "=***");
                continue;
            }
            sj.add(name + "=" + sanitize(String.valueOf(args[i])));
        }

        return sj.length() > 0 ? sj.toString() : "无";
    }

    /**
     * 判断参数名是否为密码类敏感字段。
     */
    private boolean isSensitiveParamName(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.contains("password") || lower.contains("passwd") || lower.contains("pwd");
    }

    /**
     * 对字符串中的密码类字段值做脱敏。
     * 兼容 Lombok toString 格式（newPassword=xxx）与 JSON 格式（"newPassword":"xxx"）。
     */
    private String sanitize(String text) {
        if (text == null) return "";
        // 匹配 password/passwd/pwd 及常见组合字段，脱敏其等号/冒号后的值
        return text.replaceAll("(?i)(\\b[a-z]*(?:password|passwd|pwd)[a-z]*\\s*[=:]\\s*)([^,\"}\\s]+)", "$1***");
    }
}