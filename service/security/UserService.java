package com.cb.service.security;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.security.User;

import java.util.Map;

/**
 * Created by l on 2016/11/23.
 */
public interface UserService extends CommonService<User, String> {

    /**
     * 根据用户登录帐号查找用户
     *
     * @param username 登录帐号
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 根据登录帐号和密码获得用户信息
     *
     * @param username 登录帐号
     * @param type     用户类型（系统、会员）
     * @return 用户信息
     */
    User getUser(String username, User.TypeEnum type);


    /**
     * 根据条件分页查询用户信息
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 分页查询结果
     */
    Page<User> findPageByParams(Page<User> page, Map<String, Object> params);

    /**
     * 重置密码
     *
     * @param user     用户
     * @param password 新密码
     * @return 返回修改后的用户
     */
    User resetPassword(User user, String password);
}
