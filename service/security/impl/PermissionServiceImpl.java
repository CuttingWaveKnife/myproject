package com.cb.service.security.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.dao.security.PermissionDao;
import com.cb.model.security.Permission;
import com.cb.service.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by l on 2016/11/23.
 */
@Service
@Transactional
public class PermissionServiceImpl extends CommonServiceImpl<Permission, String> implements PermissionService {

    @Autowired
    private PermissionDao permissionDao;

    @Override
    protected CommonDao<Permission, String> getCommonDao() {
        return permissionDao;
    }


}
