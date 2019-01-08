package com.cb.dao.warehouse;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DaoUtil;
import com.cb.model.warehouse.Warehouse;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class WarehouseDao extends CommonDao<Warehouse, String> {

    private static final String query = "warehouse-query";

    /**
     * 根据条件分页查询仓库
     *
     * @param page   分页信息
     * @param params 查询条件
     * @return 查询结果
     */
    public Page<Warehouse> findPageByParams(Page<Warehouse> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String hql = daoUtil.getQueryString(query, "findPageByParams_hql");
        daoUtil.put(params);
        hql = daoUtil.getParmeterSql(hql);
        return findPage(page, hql, params);
    }
}
