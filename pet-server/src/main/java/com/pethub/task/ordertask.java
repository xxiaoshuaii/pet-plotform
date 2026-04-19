package com.pethub.task;

import com.pethub.mapper.OrderMapper;
import com.pethub.pojo.entity.Orders;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ordertask {

    private final OrderMapper orderMapper;


    @Scheduled(cron = "0 * * * * ? ")
    public void cancelPayOrder() {
        log.info("开始执行定时任务：取消订单");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-5);
        List<Orders> orders = orderMapper.selectorder(time, 0);
        for (Orders order : orders) {
            orderMapper.updateStatusById(order.getId(), 3);
        }
    }

    @Scheduled(cron = "0 0 1 * * ? ")
    public void complishOrder() {
        log.info("开始执行定时任务：完成订单");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orders = orderMapper.selectorder(time, 1);
        for (Orders order : orders) {
            orderMapper.updateStatusById(order.getId(), 2);
        }
    }
}
