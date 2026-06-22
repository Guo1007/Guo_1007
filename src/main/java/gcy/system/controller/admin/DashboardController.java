package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.service.admin.IDashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Resource
    private IDashboardService dashboardService;

    @GetMapping("/stats")
    public Result stats() {
        return dashboardService.getStats();
    }

    @GetMapping("/order-trend")
    public Result orderTrend() {
        return dashboardService.getOrderTrend();
    }

    @GetMapping("/low-stock")
    public Result lowStock() {
        return dashboardService.getLowStock();
    }

    @GetMapping("/top-furniture")
    public Result topFurniture() {
        return dashboardService.getTopFurniture();
    }
}
