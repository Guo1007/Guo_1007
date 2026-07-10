package gcy.system.service.admin.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gcy.system.entity.dto.Result;
import gcy.system.entity.pojo.Order;
import gcy.system.entity.pojo.OrderItem;
import gcy.system.entity.pojo.User;
import gcy.system.entity.vo.OrderVO;
import gcy.system.exception.BusinessException;
import gcy.system.mapper.OrderMapper;
import gcy.system.mapper.UserMapper;
import gcy.system.service.IOrderItemService;
import gcy.system.service.admin.IOrderManageService;
import gcy.system.service.EmailService;
import gcy.system.utils.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static gcy.system.utils.OrderStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderManageServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements IOrderManageService {

    private final OrderMapper orderMapper;

    private final IOrderItemService orderItemService;

    private final EmailService emailService;

    private final UserMapper userMapper;

    @Override
    public Result getOrderList(Integer current, Integer size, Integer userId,
                               Integer status, String phone, String consignee) {
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StrUtil.isNotBlank(phone)) {
            wrapper.like(Order::getPhone, phone);
        }
        if (StrUtil.isNotBlank(consignee)) {
            wrapper.like(Order::getConsignee, consignee);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> resultPage = orderMapper.selectPage(page, wrapper);
        List<Order> orders = resultPage.getRecords();
        Map<Long, List<OrderItem>> itemMap = new HashMap<>();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<OrderItem> allItems = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
            itemMap.putAll(allItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderId)));
        }
        List<OrderVO> voList = orders.stream()
                .map(order -> OrderVO.from(order, itemMap.getOrDefault(order.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        Page<OrderVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(resultPage.getTotal());
        voPage.setSize(resultPage.getSize());
        voPage.setCurrent(resultPage.getCurrent());
        voPage.setPages(resultPage.getPages());
        return Result.ok(voPage);
    }

    @Override
    @Transactional
    public Result shipOrderById(Long id) {
        Order order = getById(id);
        if (order == null) {
            return Result.fail("订单不存在！");
        }
        int status = order.getStatus();
        if (status != PAID.getCode()) {
            if (status == PENDING_PAYMENT.getCode()) {
                return Result.fail("该订单还未支付！");
            } else if (status == SHIPPED.getCode() || status == COMPLETED.getCode() || status == REVIEWED.getCode()) {
                return Result.ok();
            } else {
                return Result.fail("订单已被取消！");
            }
        }
        boolean success = update()
                .set("status", SHIPPED.getCode())
                .set("ship_time", LocalDateTime.now())
                .eq("id", id)
                .eq("status", PAID.getCode())
                .update();
        if (!success) {
            Order updated = getById(id);
            if (updated.getStatus() == SHIPPED.getCode() || updated.getStatus() == COMPLETED.getCode() || updated.getStatus() == REVIEWED.getCode()) {
                return Result.ok();
            }
            throw new BusinessException("发货失败，请联系系统管理人员检查！");
        }
        sendOrderStatusEmail(order, "订单已发货",
                "您的订单 #" + order.getId() + " 已发货，请留意收货。",
                "🚚", "#3498db");
        return Result.ok();
    }

    private void sendOrderStatusEmail(Order order, String title, String content,
                                      String statusIcon, String statusColor) {
        try {
            User user = userMapper.selectById(order.getUserId());
            if (user != null && StrUtil.isNotBlank(user.getEmail())) {
                emailService.sendOrderStatusEmail(user.getEmail(), order.getId(), title, content,
                        statusIcon, statusColor, order.getTotalPrice().toString(), user.getUserName());
            }
        } catch (Exception e) {
            log.error("发送订单状态邮件失败: orderId={}", order.getId(), e);
        }
    }

    private static final DateTimeFormatter CSV_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportOrders(PrintWriter w) throws IOException {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));

        w.println("订单号,用户ID,收货人,电话,地址,金额,状态,备注,创建时间,支付时间,发货时间");
        for (Order o : orders) {
            String statusText;
            try {
                statusText = OrderStatus.fromCode(o.getStatus()).getDesc();
            } catch (IllegalArgumentException e) {
                statusText = "未知";
            }
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
        // 防止CSV公式注入
        if (val.startsWith("=") || val.startsWith("+") || val.startsWith("-") || val.startsWith("@")) {
            val = "\t" + val;
        }
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
