package com.cb.service.product.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.dao.product.ProductCategoryDao;
import com.cb.model.product.ProductCategory;
import com.cb.service.product.ProductCategoryService;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductCategoryServiceImpl extends CommonServiceImpl<ProductCategory, String> implements ProductCategoryService {

    @Autowired
    private ProductCategoryDao productCategoryDao;

    @Override
    protected CommonDao<ProductCategory, String> getCommonDao() {
        return productCategoryDao;
    }

    @Override
    public ProductCategory findByCode(String code) {
        return productCategoryDao.findUnique(Restrictions.eq("code", code));
    }
}
