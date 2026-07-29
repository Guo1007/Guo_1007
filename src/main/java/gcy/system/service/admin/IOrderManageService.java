package gcy.system.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Order;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 订单管理服务接口。
 * <p>
 * 提供订单的后台管理功能，包括订单列表查询、发货操作、订单导出，
 * 以及待发货数量统计等业务能力。
 * </p>
 *
 * @author 郭名城
 * @date 2026-07-30
 */
public interface IOrderManageService extends IService<Order> {

    /**
     * 分页查询订单列表。
     * <p>
     * 支持按用户ID、订单状态、联系电话、收货人姓名等条件进行筛选，
     * 返回分页后的订单数据。
     * </p>
     *
     * @param current   当前页码
     * @param size      每页记录数
     * @param userId    用户ID，用于按用户筛选订单，可为null表示不筛选
     * @param status    订单状态，用于按状态筛选订单，可为null表示不筛选
     * @param phone     联系电话，用于按电话模糊匹配订单，可为null表示不筛选
     * @param consignee 收货人姓名，用于按收货人模糊匹配订单，可为null表示不筛选
     * @return 包含分页订单列表及分页信息的Result对象
     */
    Result getOrderList(Integer current, Integer size, Integer userId,
                        Integer status, String phone, String consignee);

    /**
     * 根据订单ID执行发货操作。
     * <p>
     * 将指定订单的状态更新为已发货，完成订单的发货处理流程。
     * </p>
     *
     * @param id 要发货的订单ID
     * @return 包含发货操作结果的Result对象
     */
    Result shipOrderById(Long id);

    /**
     * 导出订单数据并写入到输出流。
     * <p>
     * 将所有订单数据以CSV或Excel格式写入指定的PrintWriter，
     * 供用户下载或导出。
     * </p>
     *
     * @param writer 用于写入导出数据的PrintWriter输出流
     * @throws IOException 当写入输出流发生I/O错误时抛出
     */
    void exportOrders(PrintWriter writer) throws IOException;

    /**
     * 获取当前待发货的订单数量。
     * <p>
     * 统计系统中所有状态为待发货的订单总数，用于后台管理面板的
     * 待处理事项提示。
     * </p>
     *
     * @return 包含待发货订单数量的Result对象
     */
    Result getPendingShipCount();

}
