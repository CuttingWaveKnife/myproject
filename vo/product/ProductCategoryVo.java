package com.cb.vo.product;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.product.ProductCategory;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ProductCategoryVo implements Serializable{

	/**
	 * 
	 */

	private String code;    //编码
    private String name;    //名称
    private String remark;  //备注
	
    public static ProductCategoryVo toProductCategoryVo(ProductCategory productCategory){
    	ProductCategoryVo productCategoryVo = new ProductCategoryVo();
    	BeanUtil.copyPropertiesWithoutNullValues(productCategoryVo,productCategory);
    	//额外处理
    	
    	return productCategoryVo;
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
	
}
