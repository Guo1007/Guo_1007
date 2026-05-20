package com.example.furnituresystem.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.furnituresystem.entity.dto.Result;
import com.example.furnituresystem.entity.pojo.Order;
import com.example.furnituresystem.mapper.OrderMapper;
import com.example.furnituresystem.service.admin.IOrderManageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/admin/order")
public class OrderManageController {

    @Resource
    private IOrderManageService orderManageService;

    @Resource
    private OrderMapper orderMapper;

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

    private static final DateTimeFormatter CSV_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/export")
    public void exportOrders(HttpServletResponse response) throws IOException {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime));

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=orders.csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter w = response.getWriter();
        w.println("﻿订单号,用户ID,收货人,电话,地址,金额,状态,备注,创建时间,支付时间,发货时间");
        String[] statusMap = {"待支付", "已支付", "已发货", "已完成", "已取消", "已评价"};

        for (Order o : orders) {
            String statusText = o.getStatus() >= 0 && o.getStatus() < statusMap.length
                    ? statusMap[o.getStatus()] : "未知";
            w.printf("%d,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    o.getId(),
                    o.getUserId(),
                    csvEscape(o.getConsignee()),
                    csvEscape(o.getPhone()),
                    csvEscape(o.getAddress()),
                    o.getTotalPrice(),
                    statusText,
                    csvEscape(o.getRemark()),
                    csvDate(o.getCreateTime()),
                    csvDate(o.getPayTime()),
                    csvDate(o.getShipTime()));
        }
        w.flush();
    }

    private String csvEscape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private String csvDate(LocalDateTime dt) {
        if (dt == null) return "";
        return "\t" + dt.format(CSV_DATE_FMT);
    }

}
