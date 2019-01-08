package com.cb.dao.product;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DaoUtil;
import com.cb.model.product.Product;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Created by l on 2016/12/28.
 */
@Repository
public class ProductDao extends CommonDao<Product, String> {

    private static final String QUERY = "product-query";

    /**
     * 根据分页和查询条件进行分页查询产品
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 分页查询结果集
     */
    public Page<Product> findPageByParams(Page<Product> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String hql = daoUtil.getQueryString(QUERY, "findPageByParams_hql");
        daoUtil.put(params);
        hql = daoUtil.getParmeterSql(hql);
        return findPage(page, hql);
    }

    /**
     * 后台根据分页和查询条件进行分页查询产品
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 分页查询结果集
     */
    public Page<Product> findPageByBackstage(Page<Product> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String hql = daoUtil.getQueryString(QUERY, "findPageByBackstage_hql");
        daoUtil.put(params);
        hql = daoUtil.getParmeterSql(hql);
        return findPage(page, hql);
    }

    /**
     * 统计产品的销售情况
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 分页查询结果集
     */
    public Page<Map<String, Object>> getSalesVolume(Page page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(QUERY, "findSalesVolume_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (Page<Map<String, Object>>) findPageBySql(page, sql, null, params);
    }
}
