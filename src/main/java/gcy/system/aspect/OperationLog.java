package gcy.system.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，用于标记需要记录操作日志的方法。
 * <p>
 * 配合 {@link OperationLogAspect} 切面使用，自动记录操作人、操作内容、参数及执行结果。
 * </p>
 *
 * @author 郭名城
 * @date 2026-08-03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 操作描述，如"新增商品"、"创建订单"等。
     *
     * @return 操作描述字符串
     */
    String value();
}