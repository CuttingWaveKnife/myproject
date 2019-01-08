package com.cb.model.product;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * 产品分类
 */
@Entity
@Table(name = "p_product_category")
@Where(clause = Constants.DELETED_FALSE)
public class ProductCategory extends CommonEntity {

    public enum StatusEnum {

        NORMAL("正常"), HIDDEN("屏蔽");

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

    private List<Product> products; //分类下所有产品

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

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY, mappedBy = "category")
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
