package com.cb.service.activition.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.hibernate.query.Page;
import com.cb.dao.active.RecordDao;
import com.cb.model.active.Record;
import com.cb.service.activition.RecordService;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/23.
 */
@Service
@Transactional
public class RecordServiceImpl extends CommonServiceImpl<Record, String> implements RecordService {

    @Autowired
    private RecordDao recordDao;

    @Override
    protected CommonDao getCommonDao() {
        return recordDao;
    }

    /**
     * 是否可以参与活动
     *
     * @return true： 可参与
     */
    @Override
    public Record isExist(String memberId) {
        return recordDao.findUnique(Restrictions.eq("memberId", memberId));
    }

    @Override
    public Page<Map<String, Object>> findPageByParams(Page<Map<String, Object>> page, Map<String, Object> params) {
        return recordDao.findPageByParams(page, params);
    }
}
