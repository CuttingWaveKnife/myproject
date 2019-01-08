package com.cb.model.warehouse;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import com.cb.model.product.Product;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 仓库
 */
@Entity
@Table(name = "w_warehouse_stock")
@Where(clause = Constants.DELETED_FALSE)
public class Stock extends CommonEntity {

    public enum StockStatusEnum {
        HAVD("有库存"), NO("无库存");

        private String desc;

        private StockStatusEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    private Boolean stockSync = false;  //是否管理库存

    private StockStatusEnum stockStatus = StockStatusEnum.HAVD; //库存状态

    private Integer salesNum = 0;   //销售量

    private Integer lockNum = 0;    //锁库量

    private Integer stockNum = 0;   //库存量

    private Integer leastNum = 0;   //无库存状态量

    private Integer warnNum = 0;    //最低预警库存量

    private Warehouse warehouse;    //对应仓库

    private Product product;    //对应产品

    @Column(name = "stock_sync")
    public Boolean getStockSync() {
        return stockSync;
    }

    public void setStockSync(Boolean stockSync) {
        this.stockSync = stockSync;
    }

    @Enumerated(value = EnumType.STRING)
    @Column(name = "stock_status")
    public StockStatusEnum getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(StockStatusEnum stockStatus) {
        this.stockStatus = stockStatus;
    }

    @Column(name = "sales_num")
    public Integer getSalesNum() {
        return salesNum;
    }

    public void setSalesNum(Integer salesNum) {
        this.salesNum = salesNum;
    }

    @Column(name = "lock_num")
    public Integer getLockNum() {
        return lockNum;
    }

    public void setLockNum(Integer lockNum) {
        this.lockNum = lockNum;
    }

    @Column(name = "stock_num")
    public Integer getStockNum() {
        return stockNum;
    }

    public void setStockNum(Integer stockNum) {
        this.stockNum = stockNum;
    }

    @Column(name = "least_num")
    public Integer getLeastNum() {
        return leastNum;
    }

    public void setLeastNum(Integer leastNum) {
        this.leastNum = leastNum;
    }

    @Column(name = "warn_num")
    public Integer getWarnNum() {
        return warnNum;
    }

    public void setWarnNum(Integer warnNum) {
        this.warnNum = warnNum;
    }

    @ManyToOne()
    @JoinColumn(name = "warehouse_id")
    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
