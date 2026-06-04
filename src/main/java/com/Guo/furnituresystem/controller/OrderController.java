package com.Guo.furnituresystem.controller;

import com.Guo.furnituresystem.entity.dto.CartFormDTO;
import com.Guo.furnituresystem.entity.dto.Result;
import com.Guo.furnituresystem.service.IOrderItemService;
import com.Guo.furnituresystem.service.IOrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
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
