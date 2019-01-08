package com.cb.service.order;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.order.Order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by l on 2016/12/28.
 */
public interface OrderService extends CommonService<Order, String> {

    /**
     * 根据条件统计得下单信息
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    Map<String, Object> statistics(Map<String, Object> params);

    /**
     * 会员下单排行榜
     *
     * @param params 查询条件
     * @return 返回排行信息
     */
    Map<String, Object> rank(Map<String, Object> params);

    /**
     * 根据条件统计会员活跃度信息
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    Map<String, Object> active(Map<String, Object> params);

    /**
     * 根据订单编号查询订单
     *
     * @param code 订单编号
     * @return 订单
     */
    Order findByCode(String code);

    /**
     * 管理系统根据条件分页查询订单列表
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 订单列表
     */
    Page<Order> findPageByParams(Page<Order> page, Map<String, Object> params);

    /**
     * 数据导出查询
     *
     * @param params 查询条件
     * @return 订单列表
     */
    List<Map<String, Object>> findMapByParamsAlone(Map<String, Object> params);

    /**
     * 管理系统根据会员统计订单
     * 只统计了系统单 被删除的会员也会统计
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 订单列表
     */
    Page<Map<String, Object>> findPageByMember(Page<Map<String, Object>> page, Map<String, Object> params);

    /**
     * 管理系统根据会员统计订单
     * 只统计了系统单
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 订单列表
     */
    Page<Order> findPageByMemberDetails(Page<Order> page, Map<String, Object> params);

    /**
     * 微信用户根据条件分页查找订单map列表
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 订单map列表
     */
    Page<Map<String, Object>> findMapPageByParams(Page<Map<String, Object>> page, Map<String, Object> params);

    /**
     * 查询订单金额汇总
     *
     * @param params 查询条件
     * @return 查询结果
     */
    Map<String, Object> findMapByMemberSum(Map<String, Object> params);

    /**
     * 获取待发货的订单列表(金蝶接口)
     *
     * @param username 用户帐号
     * @param password 用户密码
     * @param page     分页条件
     * @param params   查询条件
     * @return 订单map列表
     */
    Page<Map<String, Object>> getOrderList(String username, String password, Page<Map<String, Object>> page, Map<String, Object> params);

    /**
     * 创建新订单
     *
     * @param order 订单
     * @return 创建的订单
     */
    Order create(Order order);

    /**
     * 取消订单
     *
     * @param code 订单编号
     */
    void cancel(String code);

    /**
     * 代理发货
     *
     * @param code 订单编号
     */
    void deliverSelf(String code);

    /**
     * 确认付款
     *
     * @param codes 订单号，多个用逗号隔开
     */
    Order receive(String codes);

    /**
     * 上级会员审核订单
     *
     * @param code 订单编号
     */
    void auditByMember(String code);

    /**
     * 客服审核订单
     *
     * @param code       订单编号
     * @param codes      子订单编号
     * @param warehouses 仓库id
     * @param amount     实收金额
     * @param remark     备注
     * @param account    账户
     * @param source     来源
     */
    void auditByService(String code, String[] codes, String[] warehouses, BigDecimal amount, String remark, String account, String source);

    /**
     * 财务审核订单
     *
     * @param code 订单编号
     */
    void auditByFinance(String code);

    /**
     * 自动审核
     */
    void auditByAuto();

    /**
     * 确认发货(金蝶接口)
     *
     * @param username      用户帐号
     * @param password      用户密码
     * @param code          订单编号
     * @param express       快递公司
     * @param expressNumber 快递单号
     */
    void deliver(String username, String password, String code, String express, String expressNumber);

    /**
     * 确认收货
     *
     * @param code 订单编号
     */
    void receipt(String code);

    /**
     * 取消系统订单
     *
     * @param id 订单id
     */
    void revoke(String id, String remark);

    /**
     * 财务审核回退订单给客服
     *
     * @param code 订单编号
     */
    void revokeOrder(String code, String remark);

    Integer customer(Date date);
}
