package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component//注册容器
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    // 1.订单超时付款自动取消  [每分钟检查一次]
    @Scheduled(cron = "0 1 * * * ? ")
    public void processTimeoutOrder(){
        log.info("processTimeoutOrder： {}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);
        // select * from orders where status = ? and order_time < time.now - 15m
        List<Orders> ordersList =  orderMapper.getByStatusAndOrderTimeLt(Orders.PENDING_PAYMENT,localDateTime);
        for (Orders orders : ordersList) {
            // 设置订单取消状态
            orders.setStatus(Orders.CANCELLED);
            // 设置取消原因
            orders.setCancelReason("超时未支付，自动取消");
            // 设置取消时间
            orders.setCancelTime(localDateTime.now());
            // 更新数据状态
            orderMapper.update(orders);
        }
    }
    // 2.订单派送状态自动已完成 [每天凌晨1点检查一次]
    @Scheduled(cron = "0 1 1 * * ? ")
    public void processCompletedOrder(){
        log.info("processCompletedOrder： {}", LocalDateTime.now());
        // 计算前天的时间段
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLt(Orders.DELIVERY_IN_PROGRESS, localDateTime);
        // 批量检查已派送但未完成的订单
        for (Orders orders : ordersList) {
            // 设置订单取消状态
            orders.setStatus(Orders.COMPLETED);
            // 更新数据状态
            orderMapper.update(orders);
        }
    }
}
