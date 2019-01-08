package com.cb.vo.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.hibernate.query.Page;
import com.cb.common.util.ListUtil;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.active.Banner;
import com.cb.model.order.Order;
import com.cb.vo.PageVo;

/**
 * Created by GuoMIn on 2017/4/7.
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class OrderMemberVo implements Serializable {

    private String code; //订单编号

    private BigDecimal payAmount = BigDecimal.ZERO; //实际支付费用

    private String name; //用户名称

    private String head;    //会员头像

    private String level;  //会员等级

    private String status; //订单状态
    
    private String statusDesc;	//订单状态

    private Integer productNum;  //订单产品数量

    private String type;    //订单类型

    private BigDecimal income = BigDecimal.ZERO;      //应收金额

    private String imagePaths;  //产品图片地址集

    private String process;     //最新流程描述

    private String processTime;  //修改时间

    public static OrderMemberVo toOrderMemberVo(OrderMemberVo banner){
        OrderMemberVo orderMemberVo = new OrderMemberVo();
        BeanUtil.copyPropertiesWithoutNullValues(new Banner(),banner);
        return orderMemberVo;
    }

    public static PageVo<OrderMemberVo> toVoPage(Page<Map<String, Object>> page){
    	PageVo<OrderMemberVo> voPage = new PageVo<>();
        BeanUtil.copyPropertiesWithoutNullValues(voPage, page);
        List<OrderMemberVo> list = ListUtil.copyPropertiesInList(OrderMemberVo.class, page.getResult());
        voPage.setList(list);
        return voPage;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setName(String realName) {
        this.name = realName;
    }

    public String getName() {
        return name;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getHead() {
        return head;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setImagePaths(String imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getImagePaths() {
        return imagePaths;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getProcess() {
        return process;
    }

    public void setProcessTime(String processTime) {
        this.processTime = processTime;
    }

    public String getProcessTime() {
        return processTime;
    }

    public void setProductNum(Integer productNum) {
        this.productNum = productNum;
    }

    public Integer getProductNum() {
        return productNum;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

	public String getStatusDesc() {
		return Order.StatusEnum.valueOf(getStatus()).getDesc();
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
}
