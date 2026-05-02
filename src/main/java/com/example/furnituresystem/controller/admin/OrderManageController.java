package com.example.furnituresystem.controller.admin;


import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.service.admin.IOrderManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/admin/order")
public class OrderManageController {

    @Resource
    private IOrderManageService orderManageService;

    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") Integer current,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) Integer userId,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String consignee) {
        return orderManageService.getOrderList(current, size, userId, status, phone, consignee);
    }

    @PutMapping("/ship/{orderId}")
    public Result shipOrderById(@PathVariable Long orderId) {
        return orderManageService.shipOrderById(orderId);
    }

}
