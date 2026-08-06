package gcy.system.utils;

import lombok.Getter;

/**
 * 订单状态枚举类，定义了订单在业务流程中可能处于的各个状态。
 * 每个状态包含一个数字编码和对应的中文描述，便于数据库存储与前端展示。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@Getter
public enum OrderStatus {

    PENDING_PAYMENT(0, "待支付"),

    PAID(1, "已支付"),

    SHIPPED(2, "已发货"),

    COMPLETED(3, "已完成"),

    CANCELLED(4, "已取消"),

    REVIEWED(5, "已评价"),

    REFUND_APPLYING(6, "申请退款中"),

    REFUND_AUDITING(7, "退款审核中"),

    REFUNDED(8, "已退款");

    /** 订单状态编码 */
    private final int code;

    /** 订单状态中文描述 */
    private final String desc;

    /**
     * 构造函数，用于初始化订单状态的编码和描述。
     *
     * @param code 订单状态的数字编码
     * @param desc 订单状态的中文描述
     */
    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据数字编码获取对应的订单状态枚举值。
     * 该方法遍历所有枚举常量，返回与给定编码匹配的状态；若未找到则抛出异常。
     *
     * @param code 要查找的订单状态编码
     * @return 与编码对应的订单状态枚举值
     * @throws IllegalArgumentException 当编码无效（无对应状态）时抛出
     */
    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态码: " + code);
    }
}
