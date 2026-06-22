package gcy.system.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Order;

import java.io.IOException;
import java.io.PrintWriter;

public interface IOrderManageService extends IService<Order> {

    Result getOrderList(Integer current, Integer size, Integer userId,
                        Integer status, String phone, String consignee);

    Result shipOrderById(Long id);

    void exportOrders(PrintWriter writer) throws IOException;

}
