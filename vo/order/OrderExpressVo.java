package com.cb.vo.order;

import com.cb.common.util.ListUtil;
import com.cb.model.order.OrderProcess;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.List;

/**
 * Created by GuoMIn on 2017/4/8.
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class OrderExpressVo implements Serializable {

    private List<OrderProcessVo> processVoList; //订单进程列表

    private String status; //订单状态

    private String processName; //订单最新进度

    private Long processTime;    //订单最新进度时间

    private String express; //快递公司

    private String expressNumber;   //快递单号

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    public String getExpress() {
        return express;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber;
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setProcessVoList(List<OrderProcess> orderProcessList) {
        this.processVoList = ListUtil.copyPropertiesInList(OrderProcessVo.class, orderProcessList);
    }

    public List<OrderProcessVo> getProcessVoList() {
        return processVoList;
    }

    public void setProcessTime(Long processTime) {
        this.processTime = processTime;
    }

    public Long getProcessTime() {
        return processTime;
    }
}
