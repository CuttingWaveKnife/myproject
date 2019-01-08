package com.cb.vo.order;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.order.OrderProcess;

/**
 * Created by GuoMIn on 2017/4/8.
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class OrderProcessVo implements Serializable{

    private String name;  //订单进程

    private Long creationTime;  //订单进程时间
    
    private String remark;
    
    public static OrderProcessVo toOrderProcessVo(OrderProcess orderProcess){
    	OrderProcessVo orderProcessVo = new OrderProcessVo();
    	BeanUtil.copyPropertiesWithoutNullValues(orderProcessVo, orderProcess);
    	return orderProcessVo;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getCreationTime() {
        return creationTime;
    }

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
