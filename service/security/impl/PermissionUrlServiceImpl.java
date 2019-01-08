package com.cb.service.security.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.dao.security.PermissionUrlDao;
import com.cb.model.security.PermissionUrl;
import com.cb.service.security.PermissionUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by l on 2016/11/23.
 */
@Service
@Transactional
public class PermissionUrlServiceImpl extends CommonServiceImpl<PermissionUrl, String> implements PermissionUrlService {

    @Autowired
    private PermissionUrlDao permissionUrlDao;

    @Override
    protected CommonDao<PermissionUrl, String> getCommonDao() {
        return permissionUrlDao;
    }


}
