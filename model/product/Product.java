package com.cb.model.product;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import com.cb.common.util.ListUtil;
import com.cb.model.common.ImageDatabase;
import com.cb.model.member.Member;
import com.cb.model.warehouse.Stock;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 产品
 */
@Entity
@Table(name = "p_product")
@Where(clause = Constants.DELETED_FALSE)
public class Product extends CommonEntity {

    public enum StatusEnum {
        ON_THE_SHELVES("已上架"), OFF_THE_SHELVES("未上架");

        private String desc;

        private StatusEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    private StatusEnum status;  //状态

    private String name;    //名称

    private String code;    //编码

    private String materialCode;//物料编码

    private String jodCode; //京东编码

    private String weight;  //重量

    private Double grossWeight;  //商品毛重

    private String shelfLife;   //保质期

    private String origin;  //产地

    private String skinType;    //适用皮肤

    private String crowd;   //使用人群

    private String effect;  //产品功效

    private String unit;    //产品单位

    private BigDecimal price;   //价格

    private BigDecimal priceFamily; //亲情会员价格

    private BigDecimal priceGold; //金卡会员价格

    private BigDecimal pricePlatinum; //白金会员价格

    private BigDecimal priceDiamond; //钻石会员价格

    private Double discount = 1d;    //折扣

    private Integer salesNum = 0;   //销售量

    private String description; //详情介绍

    private String remark;  //备注

    private Integer sort;   //排序

    private ProductCategory category;   //产品分类

    private List<Stock> stocks; //产品库存

    private List<ImageDatabase> images; //产品宣传图

    private ImageDatabase image;   //产品封面

    @Enumerated
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
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

    @Column(name = "material_code")
    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    @Column(name = "jod_code")
    public String getJodCode() {
        return jodCode;
    }

    public void setJodCode(String jodCode) {
        this.jodCode = jodCode;
    }

    public void setGrossWeight(Double grossWeight) {
        this.grossWeight = grossWeight;
    }

    @Column(name = "gross_weight")
    public Double getGrossWeight() {
        return grossWeight;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    @Column(name = "shelf_life")
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

    @Column(name = "skin_type")
    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skinType) {
        this.skinType = skinType;
    }

    public String getCrowd() {
        return crowd;
    }

    public void setCrowd(String crowd) {
        this.crowd = crowd;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
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

    @Column(name = "price_family")
    public BigDecimal getPriceFamily() {
        return priceFamily;
    }

    public void setPriceFamily(BigDecimal priceFamily) {
        this.priceFamily = priceFamily;
    }

    @Column(name = "price_gold")
    public BigDecimal getPriceGold() {
        return priceGold;
    }

    public void setPriceGold(BigDecimal priceGold) {
        this.priceGold = priceGold;
    }

    @Column(name = "price_platinum")
    public BigDecimal getPricePlatinum() {
        return pricePlatinum;
    }

    public void setPricePlatinum(BigDecimal pricePlatinum) {
        this.pricePlatinum = pricePlatinum;
    }

    @Column(name = "price_diamond")
    public BigDecimal getPriceDiamond() {
        return priceDiamond;
    }

    public void setPriceDiamond(BigDecimal priceDiamond) {
        this.priceDiamond = priceDiamond;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    @Column(name = "sales_num")
    public Integer getSalesNum() {
        return salesNum;
    }

    public void setSalesNum(Integer salesNum) {
        this.salesNum = salesNum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @OrderBy("sort desc")
    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "foreign_id")
    @OrderBy("sort")
    @Where(clause = "type='PRODUCT'")
    public List<ImageDatabase> getImages() {
        return images;
    }

    public void setImages(List<ImageDatabase> images) {
        this.images = images;
        if (ListUtil.isNotEmpty(images)) {
            setImage(images.get(0));
        }
    }

    @Transient
    public BigDecimal getPriceByLevel(Member.LevelEnum level) {
        switch (level) {
            case FAMILY:
                return getPriceFamily();
            case GOLD:
                return getPriceGold();
            case PLATINUM:
                return getPricePlatinum();
            case DIAMOND:
                return getPriceDiamond();
            default:
                return getPrice();
        }
    }

    @Transient
    public ImageDatabase getImage() {
        return image;
    }

    public void setImage(ImageDatabase image) {
        this.image = image;
    }
}
