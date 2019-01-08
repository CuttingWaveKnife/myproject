package com.cb.dao.active;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DaoUtil;
import com.cb.model.active.Banner;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/21.
 */
@Repository
public class BannerDao extends CommonDao<Banner, String> {

    private static final String QUERY = "banner-query";

    /**
     * 查询在所有广告
     * @param page
     * @param params
     * @return
     */
    public Page<Banner> findPageByBanner(Page<Banner> page, Map<String, Object> params) {
            DaoUtil daoUtil = new DaoUtil();
            String hql = daoUtil.getQueryString(QUERY, "findPageByBanner_hql");
            daoUtil.put(params);
            hql = daoUtil.getParmeterSql(hql);
            return findPage(page, hql);
    }

    /**
     * 条件查询在有效期内所有上架的广告
     * @param page
     * @param params
     * @return
     */
    public Page<Banner> findOnLine(Page<Banner> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String hql = daoUtil.getQueryString(QUERY, "findOnLine_hql");
        daoUtil.put(params);
        hql = daoUtil.getParmeterSql(hql);
        return findPage(page, hql);
    }
}
