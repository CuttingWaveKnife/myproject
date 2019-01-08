package com.cb.vo.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.hibernate.query.Page;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.active.Banner;
import com.cb.model.product.Product;
import com.cb.vo.PageVo;
import com.cb.vo.active.BannerVo;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ProductListVo implements Serializable {

	/**
	* 
	*/
	private String name;
	private String code;
	private String unit;
	private BigDecimal price;
	private Integer stockNum;
	private String image;	
	
	public static ProductListVo toProductVo(Product product){    
		ProductListVo productVo = new ProductListVo();
    	BeanUtil.copyPropertiesWithoutNullValues(productVo,product);
    	//额外处理
    	if(product.getImage() != null){
    		productVo.setImage(product.getImage().getFilePath());
    	}    	    	
    	return productVo;
    }
	
	public static PageVo<ProductListVo> toVoPage(Page<Product> page){
    	PageVo<ProductListVo> voPage = new PageVo<>();
    	BeanUtil.copyPropertiesWithoutNullValues(voPage, page);
    	List<ProductListVo> list = new ArrayList<ProductListVo>(page.getResult().size());
    	for(Product product : page.getResult()){
    		ProductListVo productVo = toProductVo(product);
    		product.getImage().getFilePath();
    		list.add(productVo);
    	}
    	voPage.setList(list);
    	return voPage;
    }
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public Integer getStockNum() {
		return stockNum;
	}
	public void setStockNum(Integer stockNum) {
		this.stockNum = stockNum;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	
	
	
}
