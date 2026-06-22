package gcy.system.controller;

import gcy.system.entity.dto.CartFormDTO;
import gcy.system.entity.dto.Result;
import gcy.system.service.IOrderItemService;
import gcy.system.service.IOrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private IOrderService orderService;

    @Resource
    private IOrderItemService orderItemService;

    @PostMapping("/create")
    public Result createOrder(@RequestBody CartFormDTO dto) {
        return orderService.createOrder(dto);
    }

    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") Long page,
                               @RequestParam(defaultValue = "10") Long size) {
        return orderService.getOrderByUserId(page, size);
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
