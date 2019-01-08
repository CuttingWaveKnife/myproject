package com.cb.service.payment.impl;

import com.cb.common.exception.AppServiceException;
import com.cb.common.util.DateUtil;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.common.util.WeixinUtil;
import com.cb.model.member.Member;
import com.cb.model.order.Order;
import com.cb.model.payment.PaymentRecord;
import com.cb.model.security.User;
import com.cb.service.order.OrderProcessService;
import com.cb.service.order.OrderService;
import com.cb.service.payment.PaymentService;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpPayService;
import me.chanjar.weixin.mp.bean.pay.request.WxPayRefundRequest;
import me.chanjar.weixin.mp.bean.pay.request.WxPayUnifiedOrderRequest;
import me.chanjar.weixin.mp.bean.pay.result.WxPayOrderQueryResult;
import me.chanjar.weixin.mp.bean.pay.result.WxPayRefundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static com.cb.common.util.WeixinUtil.getWxService;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderProcessService orderProcessService;

    @Override
    public synchronized Map<String, Object> pay(String codes) {
        Member member = ShiroSecurityUtil.getCurrentMember();
        if (member == null) {
            throw new AppServiceException("未登录");
        }
        Map payInfo = new HashMap<>();
        Order order = orderService.receive(codes);
        if (order == null) {
            throw new AppServiceException("订单不存在");
        }
        if (!order.getCreated().getId().equals(member.getUser().getId())) {
            throw new AppServiceException("没有权限操作该订单");
        }
        if (!order.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
            throw new AppServiceException("订单{0}", order.getStatus().getDesc());
        }
        WxMpPayService wxMpPayService = getWxService().getPayService();
        if (order.getPaymentRecord() == null) {
            WxPayUnifiedOrderRequest unifiedOrderRequest = new WxPayUnifiedOrderRequest();
            unifiedOrderRequest.setBody("爱斯贝绮-美容护肤品");
            unifiedOrderRequest.setOutTradeNo(order.getCode());
            unifiedOrderRequest.setTotalFee(order.getPayAmount().multiply(new BigDecimal(100)).intValue()); //将金额由元转为分
            unifiedOrderRequest.setSpbillCreateIp(request.getRemoteAddr());
            unifiedOrderRequest.setOpenid(member.getOpenId());
            try {
                payInfo = wxMpPayService.getPayInfo(unifiedOrderRequest);
                PaymentRecord paymentRecord = new PaymentRecord();
                paymentRecord.setTradeType(WeixinUtil.getConfig().getTradeType());
                paymentRecord.setAmount(order.getPayAmount());
                paymentRecord.setCode(DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYYMMDDHHMMSSSSS));
                paymentRecord.setFeeType("CNY");
                paymentRecord.setIp(request.getRemoteAddr());
                paymentRecord.setStatus(PaymentRecord.StatusEnum.NOTPAY);
                paymentRecord.setType(PaymentRecord.TypeEnum.WECHAT);
                paymentRecord.setUser(member.getUser());
                paymentRecord.setRemark(MessageFormat.format("{0}{1}({2})对订单{3}发起支付", member.getLevel().getDesc(), member.getRealName(), member.getMobile(), order.getCode()));
                paymentRecord.setPrepayId(StringUtil.substringAfter((String) payInfo.get("package"), "prepay_id="));
                order.setPaymentRecord(paymentRecord);
                orderService.save(order);
            } catch (WxErrorException e) {
                logger.error("微信支付失败！订单号：{},原因:{}", order.getCode(), e);
                throw new AppServiceException("微信支付失败:{}", e.getMessage());
            }
        } else {
            payInfo.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000L));
            payInfo.put("nonceStr", String.valueOf(System.currentTimeMillis()));
            payInfo.put("package", "prepay_id=" + order.getPaymentRecord().getTradeNo());
            payInfo.put("signType", "MD5");
            payInfo.put("paySign", wxMpPayService.createSign(payInfo));
        }
        payInfo.put("code", order.getCode());
        return payInfo;
    }

    @Override
    public void cancel(String code) {
        Member member = ShiroSecurityUtil.getCurrentMember();
        if (member == null) {
            throw new AppServiceException("未登录");
        }
        Order order = orderService.findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在");
        }
        if (!order.getCreated().getId().equals(member.getUser().getId())) {
            throw new AppServiceException("没有权限操作该订单");
        }
        if (!order.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
            throw new AppServiceException("订单{0}", order.getStatus().getDesc());
        }
        order.setStatus(Order.StatusEnum.CANCELED);
        PaymentRecord record = order.getPaymentRecord();
        if (record != null) {
            record.setStatus(PaymentRecord.StatusEnum.CLOSED);
            record.setRemark(MessageFormat.format("{0}{1}({2})取消订单{3}支付", member.getLevel().getDesc(), member.getRealName(), member.getMobile(), order.getCode()));

        }
        for (Order c : order.getChildren()) {
            c.setStatus(Order.StatusEnum.PAYMENTING);
        }
        orderService.save(order);
        orderProcessService.save(order);
    }

    @Override
    public void cancelBySystem(String code) {
        User user = ShiroSecurityUtil.getCurrentUser();
        if (user == null) {
            throw new AppServiceException("未登录");
        }
        Order order = orderService.findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在");
        }
        if (!order.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
            throw new AppServiceException("订单{0}", order.getStatus().getDesc());
        }
        order.setStatus(Order.StatusEnum.CANCELED);
        PaymentRecord record = order.getPaymentRecord();
        if (record != null) {
            record.setStatus(PaymentRecord.StatusEnum.CLOSED);
            record.setRemark(MessageFormat.format("{0}({1})取消订单{2}支付", user.getNickname(), user.getUsername(), order.getCode()));

        }
        for (Order c : order.getChildren()) {
            c.setStatus(Order.StatusEnum.PAYMENTING);
        }
        orderService.save(order);
        orderProcessService.save(order);
    }

    @Override
    public Map<String, Object> result(String code) {
        Map<String, Object> map = new HashMap<>();
        synchronized (this) {
            Order order = orderService.findByCode(code);
            if (order != null) {
                PaymentRecord record = order.getPaymentRecord();
                if (record != null) {
                    if (record.getStatus().equals(PaymentRecord.StatusEnum.SUCCESS)) {
                        map.put("status", true);
                    } else if (record.getStatus().equals(PaymentRecord.StatusEnum.NOTPAY)) {
                        WxMpPayService wxMpPayService = getWxService().getPayService();
                        try {
                            WxPayOrderQueryResult result = wxMpPayService.queryOrder(null, code);
                            String tradeState = result.getTradeState();
                            if (StringUtil.isNotBlank(tradeState)) {
                                record.setStatus(PaymentRecord.StatusEnum.valueOf(tradeState));
                            }
                            if (record.getStatus().equals(PaymentRecord.StatusEnum.SUCCESS)) {
                                order.setStatus(Order.StatusEnum.RECEIVEING);
                                map.put("status", true);
                            } else {
                                map.put("status", false);
                            }
                            orderService.save(order);
                        } catch (Exception e) {
                            logger.error("微信支付结果查询异常,异常原因{}", e);
                        }
                    } else {
                        map.put("status", false);
                    }
                }
            }
        }
        return map;
    }

    @Override
    public void notify(String code, String tradeNo, String totalFee) {
        synchronized (this) {
            Order order = orderService.findByCode(code);
            if (order != null) {
                if (order.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
                    PaymentRecord record = order.getPaymentRecord();
                    if (record != null) {
                        if (record.getStatus().equals(PaymentRecord.StatusEnum.NOTPAY)) {
                            if (totalFee.equals(record.getAmount().toString()) && totalFee.equals(order.getPayAmount().toString())) {
                                Member member = order.getCreated().getMember();
                                order.setStatus(Order.StatusEnum.RECEIVEING);
                                record.setTradeNo(tradeNo);
                                record.setStatus(PaymentRecord.StatusEnum.SUCCESS);
                                record.setRemark(MessageFormat.format("{0}{1}({2})支付订单{3}成功", member.getLevel().getDesc(), member.getRealName(), member.getMobile(), order.getCode()));
                                order.setPaymentRecord(record);
                                orderService.save(order);
                                orderProcessService.save(order);
                            } else {
                                logger.warn("微信支付回调警告：回调时支付金额{}与订单{}应付金额{}、支付记录{}金额{}不相等！", totalFee, code, order.getPayAmount(), record.getCode(), record.getAmount());
                            }
                        } else {
                            logger.warn("微信支付回调警告：支付记录{}的状态不是待支付状态,而是【{}】", record.getCode(), record.getStatus().getDesc());
                        }
                    } else {
                        logger.warn("微信支付回调警告：订单{}未发现支付记录", code);
                    }
                } else {
                    logger.warn("微信支付回调警告：订单{}的状态有误，【{}】不能进行支付回调操作！", code, order.getStatus().getDesc());
                }
            } else {
                logger.warn("微信支付回调警告：订单{}在系统中不存在！", code);
            }
        }
    }

    @Override
    public void refund(Order order) {
        User user = ShiroSecurityUtil.getCurrentUser();
        if (user == null) {
            throw new AppServiceException("未登录");
        }
        if (order == null) {
            throw new AppServiceException("订单不存在");
        }
        try {
            int fee = order.getPayAmount().multiply(new BigDecimal(100)).intValue();
            WxPayRefundRequest refundRequest = new WxPayRefundRequest();
            refundRequest.setTransactionId(order.getPaymentRecord().getTradeNo());
            refundRequest.setOutRefundNo(order.getPaymentRecord().getCode());
            refundRequest.setRefundFee(fee);
            refundRequest.setTotalFee(fee);
            WxPayRefundResult refundResult = WeixinUtil.getWxService().getPayService().refund(refundRequest);
            logger.info("微信退款返回结果：{}", refundResult);
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setRefundCode(refundResult.getOutRefundNo());
            paymentRecord.setTradeNo(refundResult.getRefundId());
            paymentRecord.setAmount(new BigDecimal(refundRequest.getRefundFee()).divide(new BigDecimal(100)));
            paymentRecord.setTradeType(WeixinUtil.getConfig().getTradeType());
            paymentRecord.setCode(DateUtil.dateToString(DateUtil.newInstanceDate(), DateUtil.DateFormatter.FORMAT_YYYYMMDDHHMMSSSSS));
            paymentRecord.setFeeType("CNY");
            paymentRecord.setIp(request.getRemoteAddr());
            paymentRecord.setStatus(PaymentRecord.StatusEnum.REFUND);
            paymentRecord.setType(PaymentRecord.TypeEnum.WECHAT);
            paymentRecord.setUser(order.getCreated());
            paymentRecord.setRemark(MessageFormat.format("{0}({1})对订单{2}进行了退款操作", user.getNickname(), user.getUsername(), order.getCode()));
            order.setPaymentRecord(paymentRecord);
        } catch (Exception e) {
            logger.error("微信退款出错：{}", e);
            throw new AppServiceException("微信退款出错");
        }
    }
}
