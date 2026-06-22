package gcy.system.utils;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {

    PENDING_PAYMENT(0, "待支付"),

    PAID(1, "已支付"),

    SHIPPED(2, "已发货"),

    COMPLETED(3, "已完成"),

    CANCELLED(4, "已取消"),

    REVIEWED(5, "已评价");

    private final int code;

    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态码: " + code);
    }
}
