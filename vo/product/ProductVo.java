package com.cb.vo.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.common.ImageDatabase;
import com.cb.model.product.Product;
import com.cb.vo.common.ImageDatabaseVo;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ProductVo implements Serializable {

	/**
	 * 
	 */

	private String weight;  	//重量
	private String shelfLife;   //保质期
	private String origin;  	//产地
	private String crowd;   	//使用人群
	private String skinType;    //适用皮肤
	private String effect;  	//产品功效
	private String description; 	//详情介绍
	private Integer limitNumber ;	//限制数
	private String name;    		//名称
	private BigDecimal price;   	//价格
	private BigDecimal productCurrent;	//当前价格；
	private String code;    		//编码
    private List<ImageDatabaseVo> images; 	//产品宣传图
    private ImageDatabaseVo image;   			//产品封面
	
	public static ProductVo toProductVo(Product product){
		ProductVo productVo = new ProductVo();
    	BeanUtil.copyPropertiesWithoutNullValues(productVo,product);
    	//额外处理
    	productVo.setImage(ImageDatabaseVo.toImageDatabaseVo(product.getImage()));
    	if(product.getImages() != null && product.getImages().size() > 0){
    		List<ImageDatabaseVo> imageDatabaseVoList = new ArrayList<ImageDatabaseVo>(product.getImages().size());
    		for(ImageDatabase imageDatabase : product.getImages()){
    			imageDatabaseVoList.add(ImageDatabaseVo.toImageDatabaseVo(imageDatabase));
    		}
    		productVo.setImages(imageDatabaseVoList);
    	}
    	
    	return productVo;
	}
	
	
	
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getShelfLife() {
		return shelfLife;
	}
	public void setShelfLife(String shelfLife) {
		this.shelfLife = shelfLife;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getCrowd() {
		return crowd;
	}
	public void setCrowd(String crowd) {
		this.crowd = crowd;
	}
	public String getSkinType() {
		return skinType;
	}
	public void setSkinType(String skinType) {
		this.skinType = skinType;
	}
	public String getEffect() {
		return effect;
	}
	public void setEffect(String effect) {
		this.effect = effect;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getLimitNumber() {
		return limitNumber;
	}
	public void setLimitNumber(Integer limitNumber) {
		this.limitNumber = limitNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}



	public BigDecimal getProductCurrent() {
		return productCurrent;
	}



	public void setProductCurrent(BigDecimal productCurrent) {
		this.productCurrent = productCurrent;
	}



	public List<ImageDatabaseVo> getImages() {
		return images;
	}



	public void setImages(List<ImageDatabaseVo> images) {
		this.images = images;
	}



	public ImageDatabaseVo getImage() {
		return image;
	}



	public void setImage(ImageDatabaseVo image) {
		this.image = image;
	}
	
	
}
