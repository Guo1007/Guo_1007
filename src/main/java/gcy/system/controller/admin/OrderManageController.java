package gcy.system.controller.admin;

import gcy.system.entity.dto.Result;
import gcy.system.service.admin.IOrderManageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 订单管理控制器
 * <p>
 * 提供订单的查询、发货、导出、删除等后台管理接口，
 * 所有接口均挂载在 /admin/order 路径下。
 *
 * @author 郭名城
 * @date 2026-07-30
 */
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OrderManageController {

    private final IOrderManageService orderManageService;

    /**
     * 分页查询订单列表
     * <p>
     * 支持按用户ID、订单状态、收货人手机号、收货人姓名等条件进行筛选。
     *
     * @param current   当前页码，默认值为 1
     * @param size      每页大小，默认值为 10
     * @param userId    用户ID，可选，用于按用户筛选订单
     * @param status    订单状态，可选，用于按状态筛选订单
     * @param phone     收货人手机号，可选，用于模糊查询
     * @param consignee 收货人姓名，可选，用于模糊查询
     * @return 包含分页订单列表数据的结果对象
     */
    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") Integer current,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) Integer userId,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String consignee) {
        return orderManageService.getOrderList(current, size, userId, status, phone, consignee);
    }

    /**
     * 根据订单ID执行发货操作
     *
     * @param orderId 要发货的订单ID，通过路径变量传入
     * @return 包含发货操作结果的结果对象
     */
    @PutMapping("/ship/{orderId}")
    public Result shipOrderById(@PathVariable Long orderId) {
        return orderManageService.shipOrderById(orderId);
    }

    /**
     * 导出订单数据为CSV文件
     * <p>
     * 设置响应头为CSV格式，将订单数据以CSV形式写入HTTP响应输出流，
     * 触发浏览器自动下载 orders.csv 文件。
     *
     * @param response HTTP 响应对象，用于设置响应头并输出CSV文件流
     * @throws IOException 向响应流中写入数据时可能抛出的IO异常
     */
    @GetMapping("/export")
    public void exportOrders(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=orders.csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter w = response.getWriter();
        orderManageService.exportOrders(w);
    }

    /**
     * 获取待发货订单数量
     *
     * @return 包含待发货订单总数统计的结果对象
     */
    @GetMapping("/pending-count")
    public Result getPendingCount() {
        return orderManageService.getPendingShipCount();
    }

    /**
     * 根据订单ID删除单个订单
     *
     * @param orderId 要删除的订单ID，通过路径变量传入
     * @return 删除成功时返回包含成功提示的结果对象，否则返回包含失败提示的结果对象
     */
    @DeleteMapping("/{orderId}")
    public Result deleteOrder(@PathVariable Long orderId) {
        boolean success = orderManageService.removeById(orderId);
        return success ? Result.okMsg("删除成功") : Result.fail("删除失败");
    }

    /**
     * 批量删除订单
     *
     * @param ids 要删除的订单ID列表，通过请求体以JSON数组格式传入
     * @return 批量删除成功时返回包含成功提示的结果对象，否则返回包含失败提示的结果对象
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Long> ids) {
        boolean success = orderManageService.removeByIds(ids);
        return success ? Result.okMsg("批量删除成功") : Result.fail("批量删除失败");
    }

}
