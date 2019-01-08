package com.cb.service.common;

import com.cb.common.core.service.CommonService;
import com.cb.model.common.ImageDatabase;

/**
 * Created by GuoMIn on 2017/3/16.
 */
public interface ImageDatabaseService extends CommonService<ImageDatabase, String> {

    /**
     * 新增产品图
     */
    ImageDatabase add(String id,String filePath,String fileName,ImageDatabase.TypeEnum typeEnum);
}
