package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.service.admin.IOrderManageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

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

    @GetMapping("/export")
    public void exportOrders(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=orders.csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter w = response.getWriter();
        orderManageService.exportOrders(w);
    }

}
