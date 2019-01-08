package com.cb.service.product.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.StringUtil;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.dao.product.ProductDao;
import com.cb.model.product.Product;
import com.cb.service.product.ProductService;
import com.cb.vo.ResultVo;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProductServiceImpl extends CommonServiceImpl<Product, String> implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    protected CommonDao<Product, String> getCommonDao() {
        return productDao;
    }

    @Override
    public Product findByCode(String code) {
        return productDao.findUnique(Restrictions.eq("code", code));
    }

    @Override
    public List<Product> findByCodes(String[] codes) {
        return productDao.find(Restrictions.in("code", codes));
    }

    @Override
    public Page<Product> findPageByParams(Page<Product> page, Map<String, Object> params) {
        return productDao.findPageByParams(page, params);
    }

    @Override
    public Page<Product> findPageByBackstage(Page<Product> page, Map<String, Object> params) {
        return productDao.findPageByBackstage(page, params);
    }

    @Override
    public ResultVo checkSale(Product product, Integer quantity) {
        ResultVo result = new ResultVo();
        if (product.getStatus().equals(Product.StatusEnum.OFF_THE_SHELVES)) {
            result.setMessage("商品{0}已下架", product.getName());
        }/* else if (!product.getStockStatus().equals(Product.StockStatusEnum.HAVD)) {
            result.setMessage("商品{0}没有库存", product.getName());
        } else if (product.getStockNum() < quantity) {
            result.setMessage("商品{0}库存不足{1}", product.getName(), quantity + product.getUnit());
        }*/ else {
            result.success();
        }
        return result;
    }

    @Override
    public void saveProductWithSale(Product product, Integer quantity) {
        /*if (product.getStockNum() < quantity) {
            throw new AppServiceException("商品{0}库存不足{1}{2}", product.getName(), quantity.toString(), product.getUnit());
        }
        product.setSalesNum(product.getSalesNum() + quantity);
        product.setStockNum(product.getStockNum() - quantity);
        Integer stockNum = product.getStockNum();
        if (stockNum <= product.getWarnNum()) {
            //TODO  产品数量报警
        }
        if (stockNum <= product.getLeastNum()) {
            product.setStockStatus(Product.StockStatusEnum.NO);
        }
        save(product);*/
    }

    @Override
    public void saveProductWithCancel(Product product, Integer quantity) {
        /*if (product.getSalesNum() < quantity) {
            product.setSalesNum(0);
        } else {
            product.setSalesNum(product.getSalesNum() - quantity);
        }
        product.setStockNum(product.getStockNum() + quantity);
        Integer stockNum = product.getStockNum();
        if (stockNum > product.getWarnNum()) {
            //TODO  取消产品报警
        }
        if (stockNum > product.getLeastNum()) {
            product.setStockStatus(Product.StockStatusEnum.HAVD);
        }
        save(product);*/
    }

    @Override
    public Page<Map<String, Object>> getSalesVolume(Page page, Map<String, Object> params) {
        return productDao.getSalesVolume(page, params);
    }

    @Override
    public void edit(Product product) {
        //编辑已存在的商品
        if (StringUtil.isNotBlank(product.getId())) {
            Product dbproduct = productDao.get(product.getId());
            BeanUtil.copyPropertiesWithoutNullValues(dbproduct, product);
            save(dbproduct);
        }
        //新增商品
        else {
            product.setStatus(Product.StatusEnum.OFF_THE_SHELVES);//默认未上架
            /*for (ImageDatabase imageDatabase : product.getImages()){
                imageDatabase.setForeignId(product.getId());
            }*/// TODO: 2017/3/24 新增图片未关联
            save(product);
        }
    }
}
