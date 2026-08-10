package gcy.system.exception;

import lombok.Getter;

/**
 * 业务异常类，用于在业务逻辑中抛出统一的运行时异常。
 * 包含错误码和错误信息，方便前端或调用方根据错误码进行差异化处理。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    private final Integer code;

    /**
     * 使用默认错误码 500 构造业务异常。
     *
     * @param message 异常提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 使用自定义错误码和错误信息构造业务异常。
     *
     * @param code    自定义错误码
     * @param message 异常提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
