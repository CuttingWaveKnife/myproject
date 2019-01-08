package com.cb.dao.member;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DaoUtil;
import com.cb.common.util.DateUtil;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.model.member.Member;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by l on 2016/11/28.
 */
@Repository
public class MemberDao extends CommonDao<Member, String> {

    private static String query = "member-query";

    /**
     * 统计各会员等级的人数
     *
     * @return 返回统计信息
     */
    public List<Map<String, Object>> statistics() {
        return (List<Map<String, Object>>) this.queryForMap("SELECT `level` le, COUNT(m.id) num FROM u_member m where m.deleted=FALSE AND m.`status` = 'SUCCESS' GROUP BY `level`");
    }

    /**
     * 统计各会员等级的人数
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    public List<Map<String, Object>> statistics(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(query, "statistics_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }

    /**
     * 统计各会员等级的人数
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    public List<Map<String, Object>> statistics2(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(query, "statistics2_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) query(sql, null);
    }

    /**
     * 根据条件分页查询会员信息
     *
     * @param page   分页信息
     * @param params 查询条件
     * @return 查询结果
     */
    public Page<Member> findPageByParams(Page<Member> page, Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(query, "findPageByParams_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (Page<Member>) findPageBySql(page, sql, Member.class, params);
    }

    public int batchDelete(String[] ids) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(query, "batchDelete_sql");
        Map<String, Object> map = new HashMap<>();
        map.put("ids", ids);
        map.put("status", true);
        map.put("date", DateUtil.newInstanceDate());
        map.put("user", ShiroSecurityUtil.getCurrentUserId());
        daoUtil.put(map);
        sql = daoUtil.getParmeterSql(sql);
        return createNativeQuery(sql).executeUpdate();
    }

    public List<Map<String, Object>> tree(Map<String, Object> params) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(query, "findTree_sql");
        daoUtil.put(params);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) queryForMap(sql);
    }

    public List<Map<String, Object>> diamond(String mobile) {
        DaoUtil daoUtil = new DaoUtil();
        String sql = daoUtil.getQueryString(query, "diamond_sql");
        daoUtil.put("mobile", mobile);
        sql = daoUtil.getParmeterSql(sql);
        return (List<Map<String, Object>>) queryForMap(sql);
    }

}
