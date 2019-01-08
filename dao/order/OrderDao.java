package com.cb.dao.order;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DaoUtil;
import com.cb.common.util.DateUtil;
import com.cb.common.util.ListUtil;
import com.cb.model.order.Order;
import com.cb.model.payment.PaymentRecord;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderDao extends CommonDao<Order, String> {

    private static final String QUERY = "order-query";

    /**
     * 统计订单系统
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    public List<Map<String, Object>> statistics(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "statistics_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }

    /**
     * 统计订单系统
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    public List<Map<String, Object>> statistics2(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "statistics2_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }

    /**
     * 会员下单排行榜
     *
     * @param params 查询条件
     * @return 返回排行信息
     */
    public List<Map<String, Object>> rank(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "statistics_rank_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }

    /**
     * 会员活跃度统计
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    public List<Map<String, Object>> active(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "statistics_active_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }

    /**
     * 会员活跃度统计
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    public List<Map<String, Object>> active2(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "statistics_active2_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }

    /**
     * 会员活跃度统计
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    public List<Map<String, Object>> active3(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "statistics_active3_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }

    /**
     * 管理系统根据条件分页查询订单列表
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 订单列表
     */
    public Page<Order> findPageByParams(Page<Order> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String hql = daoUtil.getQueryString(QUERY, "findPageByParams_hql");
        daoUtil.put(params);
        hql = daoUtil.getParmeterSql(hql);
        return findPage(page, hql);
    }

    /**
     * 数据导出查询
     *
     * @param params 查询条件
     * @return 订单列表
     */
    public List<Map<String, Object>> findMapByParamsAlone(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "findPageByParams_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) queryForMap(sql, params);
    }

    /**
     * 按会员查询订单
     *
     * @return
     */
    public Page<Map<String, Object>> findPageByMember(Page<Map<String, Object>> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "findPageByMember_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (Page<Map<String, Object>>) findPageBySql(page, sql, null, params);
    }

    /**
     * 微信用户根据条件分页查找订单map列表
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 订单map列表
     */
    public Page<Map<String, Object>> findMapPageByParams(Page<Map<String, Object>> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "findMapPageByParams_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (Page<Map<String, Object>>) findPageBySql(page, sql, null, params);
    }

    /**
     * 开放接口查询订单列表
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 查询结果
     */
    public Page<Map<String, Object>> getOrderList(Page<Map<String, Object>> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "open_getOrderList_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (Page<Map<String, Object>>) findPageBySql(page, sql);
    }

    public Page<Order> findPageByMemberDetails(Page<Order> page, Map<String, Object> params) {
        String sql = "SELECT oo.* FROM o_order oo LEFT JOIN u_member um ON um.user_id = oo.created_id WHERE " +
                "1=1 and oo.type = :type and oo.status in (:status) and um.id = :memberId " +
                "and oo.audit_date between :auditStartTime and :auditEndTime " +
                "and oo.creation_time between :startTime and :endTime" +
                "and oo.deleted = FALSE and um.deleted = FALSE";
        DaoUtil daoUtil = new DaoUtil();
//        String sql = daoUtil.getQueryString(QUERY, "findPageByMemberDetails_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (Page<Order>) findPageBySql(page, sql);
    }

    /**
     * 查询订单金额汇总
     *
     * @param params 查询条件
     * @return 查询结果
     */
    public Map<String, Object> findMapByMemberSum(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "findMapByMemberSum_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        List<Map<String, Object>> list = (List<Map<String, Object>>) query(sql, null);
        return list.get(0);
    }

    /**
     * 查找符合自动审核条件的所有订单
     *
     * @return 订单集合
     */
    public List<Order> findByAutoAudit() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", Order.StatusEnum.RECEIVEING);
        params.put("time", DateUtil.dateToString(DateUtil.addMinutes(DateUtil.newInstanceDate(), -30), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("type", Order.TypeEnum.SYSTEM);
        params.put("payType", PaymentRecord.TypeEnum.WECHAT);
        params.put("payStatus", PaymentRecord.StatusEnum.SUCCESS);
        DaoUtil daoUtil = new DaoUtil();
        String hql = daoUtil.getQueryString(QUERY, "findByAutoAudit_hql");
        daoUtil.put(params);
        hql = daoUtil.getParmeterSql(hql);
        return find(hql);
    }

    public Integer customer(Date date) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "customer_sql");
        daoUtil.put("date", DateUtil.format(date, DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        sql = daoUtil.getParmeterSql(sql);
        List<Map<String, BigInteger>> list = (List<Map<String, BigInteger>>) queryForMap(sql);
        if (ListUtil.isNotEmpty(list)) {
            return list.get(0).get("customer").intValue();
        }
        return 0;
    }
}
