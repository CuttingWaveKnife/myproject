package com.cb.service.activition;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.active.Record;

import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/23.
 */
public interface RecordService extends CommonService<Record, String> {

    /**
     * 是否可以参与活动
     *
     * @return true 可参与
     */
    Record isExist(String memberId);

    /**
     * 是否可以参与活动
     *
     * @return true 可参与
     */
    Page<Map<String, Object>> findPageByParams(Page<Map<String, Object>> page, Map<String, Object> params);

}

