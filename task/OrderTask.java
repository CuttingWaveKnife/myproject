package com.cb.task;

import com.cb.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTask {

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 0/1 *  * * ? ")   //每一分钟执行一次
    public void myTest() {
        orderService.auditByAuto();
    }
}
