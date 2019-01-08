package com.cb.model.warehouse;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * 仓库
 */
@Entity
@Table(name = "w_warehouse")
@Where(clause = Constants.DELETED_FALSE)
public class Warehouse extends CommonEntity {

    public enum StatusEnum {

        NORMAL("正常"), DISUSE("废弃");

        private String desc;

        private StatusEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    private StatusEnum status;  //状态

    private String code;    //编码

    private String name;    //名称

    private String remark;  //备注

    private List<Stock> stocks; //仓库下所有库存信息

    @Enumerated
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @OrderBy("sort desc")
    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }
}
