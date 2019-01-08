package com.cb.service.product;

import com.cb.common.core.service.CommonService;
import com.cb.model.product.ProductCategory;

/**
 * Created by l on 2016/12/28.
 */
public interface ProductCategoryService extends CommonService<ProductCategory, String> {

    /**
     * 根据分类编码查询分类
     *
     * @param code 分类编码
     * @return 分类
     */
    ProductCategory findByCode(String code);
}
