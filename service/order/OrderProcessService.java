package com.cb.service.order;

import com.cb.common.core.service.CommonService;
import com.cb.model.order.Order;
import com.cb.model.order.OrderProcess;

public interface OrderProcessService extends CommonService<OrderProcess, String> {

    /**
     * 保存订单操作流程
     *
     * @param order 订单
     */
    void save(Order order);
}
