package com.cb.service.security.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.dao.security.RoleDao;
import com.cb.model.security.Role;
import com.cb.service.security.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by l on 2016/11/23.
 */
@Service
@Transactional
public class RoleServiceImpl extends CommonServiceImpl<Role, String> implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Override
    protected CommonDao<Role, String> getCommonDao() {
        return roleDao;
    }

}
