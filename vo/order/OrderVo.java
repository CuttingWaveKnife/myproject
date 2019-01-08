package com.cb.vo.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.order.Order;
import com.cb.model.order.OrderProcess;
import com.cb.model.order.OrderProduct;

/**
 * Created by GuoMIn on 2017/4/14.
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class OrderVo implements Serializable {

    private String code;    //编号

    private Integer productNum; //商品数量

    private String status;  //状态

    private String type;  //类型

    private String pay;    //支付方式

    private BigDecimal income = BigDecimal.ZERO;  //应收金额

    private BigDecimal amount = BigDecimal.ZERO;  //商品总金额

    private BigDecimal postage = BigDecimal.ZERO; //邮费

    private BigDecimal discount = BigDecimal.ZERO;    //优惠费用

    private BigDecimal payAmount = BigDecimal.ZERO;   //实际支付费用

    private Integer score = 0;  //获得积分

    private Integer useScore = 0;   //使用积分

    private Double weight;  //产品重量

    private String remark;  //备注

    private String source;  //会员付款来源

    private String account;  //会员付款帐号

    private String name;    //收货人

    private String mobile;  //收货人电话

    private String province;    //省

    private String provinceName;    //省份中文名

    private String city;    //市

    private String cityName;    //城市中文名

    private String area;    //区

    private String areaName;    //区域中文名

    private String detail;  //详细地址

    private String full;    //地址全称

    private String express; //快递公司

    private String expressNumber;   //快递单号

    private String imagePaths;  //产品图片地址集

    private String process; //最新流程描述
    
    private List<OrderProductVo> productVos;    //订单产品集合

    private List<OrderProcessVo> processeVos;   //订单流程

    public static OrderVo toOrderVo(Order order){
    	OrderVo orderVo = new OrderVo();
    	BeanUtil.copyPropertiesWithoutNullValues(orderVo, order);
    	
    	//额外处理
    	if(null != order.getProducts() && order.getProducts().size() > 0){
    		List<OrderProductVo> list = new ArrayList<OrderProductVo>(order.getProducts().size());
    		for(OrderProduct orderProduct : order.getProducts()){
    			OrderProductVo orderProductVo =	OrderProductVo.toOrderProductVo(orderProduct);
    			list.add(orderProductVo);
    		}
    		orderVo.setProductVos(list);
    	}
    	
    	if(null != order.getProcesses() && order.getProcesses().size() > 0){
    		List<OrderProcessVo> list = new ArrayList<OrderProcessVo>(order.getProcesses().size());
    		for(OrderProcess orderProcess : order.getProcesses()){
    			OrderProcessVo orderProcessVo = OrderProcessVo.toOrderProcessVo(orderProcess);
    			list.add(orderProcessVo);    			
    		}
    		orderVo.setProcesseVos(list);
    	}
    	   	
    	return orderVo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getProductNum() {
        return productNum;
    }

    public void setProductNum(Integer productNum) {
        this.productNum = productNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPostage() {
        return postage;
    }

    public void setPostage(BigDecimal postage) {
        this.postage = postage;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getUseScore() {
        return useScore;
    }

    public void setUseScore(Integer useScore) {
        this.useScore = useScore;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public String getExpress() {
        return express;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber;
    }

    public String getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(String imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

	public List<OrderProductVo> getProductVos() {
		return productVos;
	}

	public void setProductVos(List<OrderProductVo> productVos) {
		this.productVos = productVos;
	}

	public List<OrderProcessVo> getProcesseVos() {
		return processeVos;
	}

	public void setProcesseVos(List<OrderProcessVo> processeVos) {
		this.processeVos = processeVos;
	}
}
