package com.cb.service.member.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.hibernate.query.Page;
import com.cb.dao.member.ScoreRecordDao;
import com.cb.model.member.ScoreRecord;
import com.cb.service.member.ScoreRecordService;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by l on 2016/11/28.
 */
@Service
@Transactional
public class ScoreRecordServiceImpl extends CommonServiceImpl<ScoreRecord, String> implements ScoreRecordService {

    @Autowired
    private ScoreRecordDao scoreRecordDao;

    @Override
    protected CommonDao<ScoreRecord, String> getCommonDao() {
        return scoreRecordDao;
    }

    @Override
    public Page<ScoreRecord> findPageByMemberId(Page<ScoreRecord> page, String memberId) {
        return scoreRecordDao.findPage(page, Restrictions.eq("member.id", memberId));
    }
}
