package com.cb.vo.order;

import java.io.Serializable;
import java.math.BigDecimal;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.order.OrderProduct;
import com.cb.vo.product.ProductVo;

/**
 * Created by GuoMIn on 2017/4/11.
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class OrderProductVo implements Serializable{

    private Integer quantity;   //数量

    private String unit;    //单位

    private BigDecimal unitPrice;   //单价

    private BigDecimal amount;  //总计

    private String code; //产品编码
    
    private ProductVo productVo;    //对应产

    public static OrderProductVo toOrderProductVo(OrderProduct orderProduct){
    	OrderProductVo orderProductVo = new OrderProductVo();
    	BeanUtil.copyPropertiesWithoutNullValues(orderProductVo, orderProduct);
    	//额外
    	if(null != orderProduct.getProduct()){
    		ProductVo productVo = ProductVo.toProductVo(orderProduct.getProduct());
    		orderProductVo.setProductVo(productVo);
    	}    	
    	return orderProductVo;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

	public ProductVo getProductVo() {
		return productVo;
	}

	public void setProductVo(ProductVo productVo) {
		this.productVo = productVo;
	}
}
