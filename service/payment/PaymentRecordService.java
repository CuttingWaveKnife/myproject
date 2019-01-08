package com.cb.service.payment;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.payment.PaymentRecord;

import java.util.List;
import java.util.Map;

public interface PaymentRecordService extends CommonService<PaymentRecord, String> {

    /**
     * 根据流水号查询记录
     *
     * @param code 流水号
     * @return 支付记录
     */
    PaymentRecord findByCode(String code);

    /**
     * 管理系统根据条件分页查询支付记录列表
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 支付记录列表
     */
    Page<PaymentRecord> findPageByParams(Page<PaymentRecord> page, Map<String, Object> params);/**
     * 管理系统根据条件分页查询支付记录列表
     *
     * @param params 查询条件
     * @return 支付记录列表
     */
    List<Map<String, Object>> findPageByParamsStatus(Map<String, Object> params);
}
