package com.cb.service.member;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.member.ScoreRecord;

/**
 * Created by l on 2016/11/28.
 */
public interface ScoreRecordService extends CommonService<ScoreRecord, String> {

    Page<ScoreRecord> findPageByMemberId(Page<ScoreRecord> page, String memberId);
}
