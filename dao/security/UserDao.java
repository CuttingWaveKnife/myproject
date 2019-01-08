package com.cb.dao.security;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.StringUtil;
import com.cb.model.security.User;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Created by l on 2016/11/23.
 */
@Repository
public class UserDao extends CommonDao<User, String> {

    /**
     * 根据条件分页查询用户信息
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 分页查询结果
     */
    public Page<User> findPageByParams(Page<User> page, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder("select u from User u inner join u.roles r where 1=1");
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (StringUtil.isNotBlank(entry.getKey()) && StringUtil.isNotBlank(entry.getValue())) {
                    if (entry.getKey().equals("search")) {
                        sb.append(" and u.username LIKE '");
                        sb.append(entry.getValue());
                        sb.append("%'");
                    } else {
                        sb.append(" and ");
                        sb.append(entry.getKey());
                        sb.append(" = :");
                        sb.append(entry.getKey());
                    }
                }
            }
        }
        return this.findPage(page, sb.toString(), params);
    }
}
