package com.cb.service.order.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.dao.order.OrderProductDao;
import com.cb.model.order.OrderProduct;
import com.cb.service.order.OrderProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderProductServiceImpl extends CommonServiceImpl<OrderProduct, String> implements OrderProductService {

    @Autowired
    private OrderProductDao orderProductDao;

    @Override
    protected CommonDao<OrderProduct, String> getCommonDao() {
        return orderProductDao;
    }

}
