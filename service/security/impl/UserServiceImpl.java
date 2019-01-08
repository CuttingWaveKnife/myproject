package com.cb.service.security.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.exception.AppServiceException;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.Constants;
import com.cb.common.util.StringUtil;
import com.cb.dao.security.UserDao;
import com.cb.model.security.User;
import com.cb.service.security.UserService;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Created by l on 2016/11/23.
 */
@Service
@Transactional
public class UserServiceImpl extends CommonServiceImpl<User, String> implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    protected CommonDao<User, String> getCommonDao() {
        return userDao;
    }

    @Override
    public User getUserByUsername(String username) {
        return userDao.findUnique(Restrictions.eq("username", username));
    }

    @Override
    public User getUser(String username, User.TypeEnum type) {
        return userDao.findUnique(Restrictions.eq("username", username), Restrictions.eq("type", type));
    }

    @Override
    public Page<User> findPageByParams(Page<User> page, Map<String, Object> params) {
        return userDao.findPageByParams(page, params);
    }

    @Override
    public User resetPassword(User user, String password) {
        if (user == null) {
            throw new AppServiceException("用户不存在");
        }
        String salt = StringUtil.getSalt();
        user.setPassword(new SimpleHash(Constants.MD5, password, salt).toString());
        user.setSalt(salt);
        save(user);
        return user;
    }
}
