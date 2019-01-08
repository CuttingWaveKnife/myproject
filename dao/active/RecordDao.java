package com.cb.dao.active;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DaoUtil;
import com.cb.model.active.Record;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/23.
 */
@Repository
public class RecordDao extends CommonDao<Record, String> {

    private static final String QUERY = "record-query";

    public Page<Map<String, Object>> findPageByParams(Page<Map<String, Object>> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "findPageByParams_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (Page<Map<String, Object>>) findPageBySql(page, sql, null, params);
    }
}
