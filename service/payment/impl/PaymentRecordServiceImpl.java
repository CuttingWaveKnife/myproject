package com.cb.service.payment.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.hibernate.query.Page;
import com.cb.dao.payment.PaymentRecordDao;
import com.cb.model.payment.PaymentRecord;
import com.cb.service.payment.PaymentRecordService;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PaymentRecordServiceImpl extends CommonServiceImpl<PaymentRecord, String> implements PaymentRecordService {

    @Autowired
    private PaymentRecordDao paymentRecordDao;

    @Override
    protected CommonDao<PaymentRecord, String> getCommonDao() {
        return paymentRecordDao;
    }

    @Override
    public PaymentRecord findByCode(String code) {
        return paymentRecordDao.findUnique(Restrictions.eq("code", code));
    }

    @Override
    public Page<PaymentRecord> findPageByParams(Page<PaymentRecord> page, Map<String, Object> params) {
        return paymentRecordDao.findPageByParams(page, params);
    }
    @Override
    public List<Map<String, Object>> findPageByParamsStatus(Map<String, Object> params) {
        return paymentRecordDao.findPageByParamsStatus(params);
    }
}
