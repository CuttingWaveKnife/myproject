package com.cb.service.common.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.dao.common.ImageDatabaseDao;
import com.cb.model.common.ImageDatabase;
import com.cb.service.common.ImageDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by GuoMIn on 2017/3/16.
 */
@Service
@Transactional
public class ImageDatabaseServiceImpl extends CommonServiceImpl<ImageDatabase, String> implements ImageDatabaseService {

    @Autowired
    private ImageDatabaseDao imageDatabaseDao;

    @Override
    protected CommonDao getCommonDao() {
        return imageDatabaseDao;
    }

    @Override
    public ImageDatabase add(String id, String filePath, String fileName, ImageDatabase.TypeEnum typeEnum) {
        ImageDatabase imageDatabase = new ImageDatabase();
        imageDatabase.setForeignId(id);
        imageDatabase.setName("file.jpg");
        imageDatabase.setUser(ShiroSecurityUtil.getCurrentUser());
        imageDatabase.setFileType("image/JPEG");
        imageDatabase.setFilePath(filePath);
        imageDatabase.setFileName(fileName);
        imageDatabase.setType(typeEnum);
        imageDatabase.setSort(0);
        save(imageDatabase);
        return imageDatabase;
    }
}
