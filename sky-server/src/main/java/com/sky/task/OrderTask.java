package com.sky.task;

import com.github.pagehelper.Page;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单的方法：每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ? ")//每分钟触发一次
    public void processTimeOutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        //select * from orders where status=? and order_time<?(当前时间-15min)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,
                                                                    LocalDateTime.now().plusMinutes(-15));

        if (ordersList!=null&& !ordersList.isEmpty()){
            for (Orders orders:ordersList){
                orders.setStatus(Orders.CANCELLED);//把状态设为取消
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处去派送中状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点触发一次
    public void processDeliveryOrder(){
        log.info("处理一直处去派送中状态的订单:{}",LocalDateTime.now());

        //select * from orders where status=? and order_time<?(当前时间-1h)

        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,
                                                                        LocalDateTime.now().plusMinutes(-60));

        if (ordersList!=null&& !ordersList.isEmpty()){
            for (Orders orders:ordersList){
                orders.setStatus(Orders.COMPLETED);//把状态设为已完成
                orderMapper.update(orders);
            }
        }
    }
}
