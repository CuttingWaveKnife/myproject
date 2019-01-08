package com.cb.service.warehouse.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.exception.AppServiceException;
import com.cb.dao.warehouse.StockDao;
import com.cb.model.product.Product;
import com.cb.model.warehouse.Stock;
import com.cb.service.warehouse.StockService;
import com.cb.vo.ResultVo;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StockServiceImpl extends CommonServiceImpl<Stock, String> implements StockService {

    @Autowired
    private StockDao stockDao;

    @Override
    protected CommonDao<Stock, String> getCommonDao() {
        return stockDao;
    }

    @Override
    public Stock findByProductandWarehouse(String productId, String warehouseId) {
        return stockDao.findUnique(Restrictions.eq("product.id", productId), Restrictions.eq("warehouse.id", warehouseId));
    }

    @Override
    public ResultVo check(Stock stock, Integer quantity) {
        ResultVo result = new ResultVo();
        if (stock != null) {
            Product product = stock.getProduct();
            if (!stock.getStockStatus().equals(Stock.StockStatusEnum.HAVD)) {
                result.setMessage("商品{0}没有库存", product.getName());
            } else if (stock.getStockNum() < quantity) {
                result.setMessage("商品{0}库存不足{1}", product.getName(), quantity + product.getUnit());
            } else {
                result.success();
            }
        } else {
            result.setMessage("找不到库存信息");
        }
        return result;
    }

    @Override
    public void saveWithSale(Stock stock, Integer quantity) {
        if (stock != null) {
            Product product = stock.getProduct();
            if (stock.getStockNum() < quantity) {
                throw new AppServiceException("商品{0}库存不足{1}{2}", product.getName(), quantity.toString(), product.getUnit());
            }
            stock.setSalesNum(stock.getSalesNum() + quantity);
            product.setSalesNum(product.getSalesNum() + quantity);
            stock.setStockNum(stock.getStockNum() - quantity);
            stock.setLockNum(stock.getLockNum() + quantity);
            Integer stockNum = stock.getStockNum();
            if (stockNum <= stock.getWarnNum()) {
                //TODO  产品数量报警
            }
            if (stockNum <= stock.getLeastNum()) {
                stock.setStockStatus(Stock.StockStatusEnum.NO);
            }
            stock.setProduct(product);
            save(stock);
        }
    }

    @Override
    public void saveWithCancel(Stock stock, Integer quantity) {
        if (stock != null) {
            Product product = stock.getProduct();
            stock.setSalesNum(stock.getSalesNum() - quantity);
            product.setSalesNum(product.getSalesNum() - quantity);
            stock.setStockNum(stock.getStockNum() + quantity);
            stock.setLockNum(stock.getLockNum() - quantity);
            Integer stockNum = stock.getStockNum();
            if (stockNum > stock.getWarnNum()) {
                //TODO  取消产品报警
            }
            if (stockNum > stock.getLeastNum()) {
                stock.setStockStatus(Stock.StockStatusEnum.HAVD);
            }
            stock.setProduct(product);
            save(stock);
        }
    }

    @Override
    public void saveWithDeliver(Stock stock, Integer quantity) {
        if (stock != null) {
            stock.setLockNum(stock.getLockNum() - quantity);
            save(stock);
        }
    }
}
