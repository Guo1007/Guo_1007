package gcy.system.controller;

import gcy.system.entity.dto.CartFormDTO;
import gcy.system.entity.dto.Result;
import gcy.system.service.IOrderItemService;
import gcy.system.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    private final IOrderItemService orderItemService;

    @PostMapping("/create")
    public Result createOrder(@RequestBody CartFormDTO dto) {
        return orderService.createOrder(dto);
    }

    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        return orderService.getOrderByUserId(page.longValue(), size.longValue());
    }

    @GetMapping("/detail/{orderId}")
    public Result getOrderDetail(@PathVariable Long orderId) {
        return orderItemService.getOrderDetail(orderId);
    }

    @PutMapping("/pay/{orderId}")
    public Result payOrder(@PathVariable Long orderId) {
        return orderService.payOrderById(orderId);
    }

    @PutMapping("/cancel/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    @PutMapping("/confirm/{orderId}")
    public Result confirmReceipt(@PathVariable Long orderId) {
        return orderService.confirmReceipt(orderId);
    }

}
