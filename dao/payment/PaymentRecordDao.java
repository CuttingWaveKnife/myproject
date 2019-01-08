package com.cb.dao.payment;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DaoUtil;
import com.cb.model.payment.PaymentRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PaymentRecordDao extends CommonDao<PaymentRecord, String> {

    private static final String QUERY = "payment-query";

    /**
     * 管理系统根据条件分页查询支付记录列表
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 支付记录列表
     */
    public Page<PaymentRecord> findPageByParams(Page<PaymentRecord> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String hql = daoUtil.getQueryString(QUERY, "findPageByParams_hql");
        daoUtil.put(params);
        hql = daoUtil.getParmeterSql(hql);
        return findPage(page, hql);
    }
    /**
     * 管理系统根据条件分页查询支付记录列表
     *
     * @param params 查询条件
     * @return 支付记录列表
     */
    public List<Map<String, Object>> findPageByParamsStatus(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = "SELECT\n" +
                "\tum.real_name realName,\n" +
                "\too.`code` code,\n" +
                "IF (\n" +
                "\tpr.type = 'WECHAT',\n" +
                "\t'微信支付',\n" +
                "\t'线下支付'\n" +
                ") type,\n" +
                " pr. CODE prCode,\n" +
                " pr.trade_no tradeNo,\n" +
                " pr.amount amount,\n" +
                "\n" +
                "IF (\n" +
                "\tpr.`status` = 'SUCCESS',\n" +
                "\t'支付成功',\n" +
                "\n" +
                "IF (\n" +
                "\tpr.`status` = 'REFUND',\n" +
                "\t'转入退款',\n" +
                "\n" +
                "IF (\n" +
                "\tpr.`status` = 'NOTPAY',\n" +
                "\t'未支付',\n" +
                "\n" +
                "IF (\n" +
                "\tpr.`status` = 'CLOSED',\n" +
                "\t'已关闭',\n" +
                "\n" +
                "IF (\n" +
                "\tpr.`status` = 'REVOKED',\n" +
                "\t'已撤销',\n" +
                "\n" +
                "IF (\n" +
                "\tpr.`status` = 'USERPAYING',\n" +
                "\t'用户支付中',\n" +
                "\n" +
                "IF (\n" +
                "\tpr.`status` = 'PAYERROR',\n" +
                "\t'支付失败',\n" +
                "\t''\n" +
                ")\n" +
                ")\n" +
                ")\n" +
                ")\n" +
                ")\n" +
                ")\n" +
                ") status,\n" +
                " pr.modification_time modificationTime,\n" +
                "\too.finance_date financeDate\n" +
                "FROM\n" +
                "\tp_payment_record pr " +
                "LEFT JOIN u_member um ON pr.user_id = um.user_id\n" +
                "LEFT JOIN o_order oo ON pr.id = oo.payment_id\n" +
                "WHERE\n" +
                "pr.`status` in (:status)" +
                " and pr.modification_time between :modificationStartTime and :modificationEndTime";
//        String sql = daoUtil.getQueryString(QUERY, "findPageByParams_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }
}
