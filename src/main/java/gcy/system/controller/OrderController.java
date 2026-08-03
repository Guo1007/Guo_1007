package gcy.system.controller;

import gcy.system.entity.dto.CartFormDTO;
import gcy.system.entity.dto.Result;
import gcy.system.service.IOrderItemService;
import gcy.system.service.IOrderService;
import gcy.system.aspect.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器，处理订单相关的HTTP请求。
 * <p>
 * 提供订单的创建、查询列表、查看详情、支付、取消、确认收货及删除等功能。
 * 所有接口均返回统一的 {@link Result} 响应格式。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    private final IOrderItemService orderItemService;

    /**
     * 创建订单。
     * <p>
     * 接收购物车表单数据，调用订单服务生成新的订单记录。
     * </p>
     *
     * @param dto 购物车表单数据传输对象，包含用户选购的商品信息
     * @return 包含创建结果的统一响应对象
     */
    @OperationLog("创建订单")
    @PostMapping("/create")
    public Result createOrder(@RequestBody CartFormDTO dto) {
        return orderService.createOrder(dto);
    }

    /**
     * 获取当前用户的订单列表，支持分页查询。
     *
     * @param page 页码，默认为第1页
     * @param size 每页条数，默认为10条
     * @return 包含当前用户分页订单数据的统一响应对象
     */
    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        return orderService.getOrderByUserId(page.longValue(), size.longValue());
    }

    /**
     * 根据订单ID获取订单详情。
     *
     * @param orderId 订单ID，用于标识唯一订单
     * @return 包含指定订单详细信息的统一响应对象
     */
    @GetMapping("/detail/{orderId}")
    public Result getOrderDetail(@PathVariable Long orderId) {
        return orderItemService.getOrderDetail(orderId);
    }

    /**
     * 支付指定订单。
     * <p>
     * 将订单状态变更为已支付。
     * </p>
     *
     * @param orderId 待支付的订单ID
     * @return 包含支付操作结果的统一响应对象
     */
    @OperationLog("支付订单")
    @PutMapping("/pay/{orderId}")
    public Result payOrder(@PathVariable Long orderId) {
        return orderService.payOrderById(orderId);
    }

    /**
     * 取消指定订单。
     *
     * @param orderId 待取消的订单ID
     * @return 包含取消操作结果的统一响应对象
     */
    @OperationLog("取消订单")
    @PutMapping("/cancel/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    /**
     * 确认收货。
     * <p>
     * 将订单状态变更为已完成，表示用户已收到商品。
     * </p>
     *
     * @param orderId 待确认收货的订单ID
     * @return 包含确认收货操作结果的统一响应对象
     */
    @PutMapping("/confirm/{orderId}")
    public Result confirmReceipt(@PathVariable Long orderId) {
        return orderService.confirmReceipt(orderId);
    }

    /**
     * 删除指定订单。
     *
     * @param orderId 待删除的订单ID
     * @return 包含删除操作结果的统一响应对象
     */
    @DeleteMapping("/{orderId}")
    public Result deleteOrder(@PathVariable Long orderId) {
        return orderService.deleteMyOrder(orderId);
    }

}
