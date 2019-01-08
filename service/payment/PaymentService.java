package com.cb.service.payment;

import com.cb.model.order.Order;

import java.util.Map;

public interface PaymentService {

    /**
     * 生成微信预支付信息
     *
     * @param code 订单编号
     */
    Map<String, Object> pay(String code);

    /**
     * 微信取消支付
     *
     * @param code 订单编号
     */
    void cancel(String code);

    /**
     * 管理系统取消微信支付
     *
     * @param code 订单编号
     */
    void cancelBySystem(String code);

    /**
     * 微信支付结果查询
     *
     * @param code 订单编号
     * @return 返回结果
     */
    Map<String, Object> result(String code);

    /**
     * 微信支付回调
     *
     * @param code     订单编号
     * @param tradeNo  微信支付编号
     * @param totalFee 微信支付金额
     */
    void notify(String code, String tradeNo, String totalFee);

    /**
     * 微信退款
     *
     * @param order 订单
     */
    void refund(Order order);
}
