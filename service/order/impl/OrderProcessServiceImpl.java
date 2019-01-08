package com.cb.service.order.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.exception.AppServiceException;
import com.cb.common.util.PropertiesUtil;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.dao.order.OrderProcessDao;
import com.cb.model.order.Order;
import com.cb.model.order.OrderProcess;
import com.cb.service.order.OrderProcessService;
import com.cb.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;

@Service
@Transactional
public class OrderProcessServiceImpl extends CommonServiceImpl<OrderProcess, String> implements OrderProcessService {

    @Autowired
    private OrderProcessDao orderProcessDao;

    @Autowired
    private OrderService orderService;

    @Override
    protected CommonDao<OrderProcess, String> getCommonDao() {
        return orderProcessDao;
    }

    @Override
    public void save(Order order) {
        try {
            OrderProcess orderProcess = new OrderProcess();
            orderProcess.setOrder(order);   //关联订单
            orderProcess.setUser(ShiroSecurityUtil.getCurrentUser());
            String process = null;
            //判断订单的类型，单列、合并、系统，根据订单类型不同获取不同的订单流程描述
            switch (order.getType()) {
                case ALONE:
                    process = order.getStatus().getProcessAlone();
                    //订单状态是“审核中”
                    if (order.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
                        String[] processes = process.split(",");    //有多种流程说明，用“，”分隔
                        if (order.getUser().getMember().getAgent() != null) {
                            process = processes[0];
                        } else {
                            process = processes[1];
                        }
                    } else if (order.getStatus().equals(Order.StatusEnum.AUDITING)) {
                        String[] processes = process.split(",");    //有多种流程说明，用“，”分隔
                        Order parent = order.getParent();
                        if (parent != null) {   //订单有父级，则标识此订单已经提交到上级审核
                            if (parent.getType().equals(Order.TypeEnum.MERGE)) {
                                if (parent.getStatus().equals(Order.StatusEnum.RECEIVEING)) {   //上级订单还未确认收款
                                    process = processes[0];
                                } else if (parent.getStatus().equals(Order.StatusEnum.PAYMENTING)) {    //上级订单还未向其上级付款
                                    process = processes[1];
                                }
                                parent = parent.getParent();    //订单有两层父级，则标识此订单已由上级提交到其上级审核
                                if (parent != null) {
                                    if (parent.getType().equals(Order.TypeEnum.MERGE)) {
                                        if (parent.getStatus().equals(Order.StatusEnum.RECEIVEING)) {
                                            process = processes[2];
                                        } else if (parent.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
                                            process = processes[3];
                                        }
                                        parent = parent.getParent();    //订单有三层父级，则标识此订单已由上上级提交到公司审核
                                        if (parent != null) {
                                            if (parent.getStatus().equals(Order.StatusEnum.RECEIVEING)) {
                                                process = processes[4];
                                            }
                                        }
                                    } else if (parent.getType().equals(Order.TypeEnum.SYSTEM)) {
                                        process = processes[4];
                                    }
                                }
                            } else if (parent.getType().equals(Order.TypeEnum.SYSTEM)) {
                                process = processes[5];
                            }
                        }
                    } else if (order.getStatus().equals(Order.StatusEnum.SELF)) {   //订单状态是“自己发货”
                        String[] processes = process.split(",");
                        Order parent = order.getParent();
                        if (parent != null) {
                            process = processes[0]; //订单有父级，则标识由上级发货
                            parent = parent.getParent();
                            if (parent != null) {
                                process = processes[1]; //订单有两层父级，则标识订单将由你的钻石会员发货
                            }
                        }
                    }
                    break;
                case MERGE:
                    process = order.getStatus().getProcessMerge();
                    if (order.getStatus().equals(Order.StatusEnum.RECEIVEING)) {
                        String[] processes = process.split(",");    //有多种流程说明，用“，”分隔
                        if (order.getUser().getMember().getAgent() != null) {
                            process = processes[0];
                        } else {
                            process = processes[1];
                        }
                        for (Order child : order.getChildren()) {   //遍历保存其子订单流程
                            save(child);
                        }
                    } else if (order.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
                        String[] processes = process.split(",");    //有多种流程说明，用“，”分隔
                        if (order.getUser().getMember().getAgent() != null) {
                            process = processes[0];
                        } else {
                            process = processes[1];
                        }
                        for (Order child : order.getChildren()) {   //遍历保存其子订单流程
                            save(child);
                        }
                    } else if (order.getStatus().equals(Order.StatusEnum.AUDITING)) {
                        String[] processes = process.split(",");
                        Order parent = order.getParent();
                        if (parent != null) {
                            if (parent.getType().equals(Order.TypeEnum.MERGE)) {
                                if (parent.getStatus().equals(Order.StatusEnum.RECEIVEING)) {
                                    process = processes[0];
                                } else if (parent.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
                                    process = processes[1];
                                }
                                parent = parent.getParent();
                                if (parent != null) {
                                    if (parent.getStatus().equals(Order.StatusEnum.RECEIVEING)) {
                                        process = processes[2];
                                    }
                                }
                            } else if (parent.getType().equals(Order.TypeEnum.SYSTEM)) {
                                process = processes[3];
                            }
                        }

                        for (Order child : order.getChildren()) {
                            save(child);
                        }
                    } else if (order.getStatus().equals(Order.StatusEnum.SELF)) {
                        String[] processes = process.split(",");
                        Order parent = order.getParent();
                        process = processes[0];
                        if (parent != null) {
                            process = processes[1];
                        }
                    }
                    break;
                case SYSTEM:
                    process = order.getStatus().getProcessSystem();//系统单进度
                    if (order.getStatus().equals(Order.StatusEnum.RECEIVEING)) {
                        for (Order child : order.getChildren()) {
                            save(child);
                        }
                    } else if (order.getStatus().equals(Order.StatusEnum.AUDITING)) {
                        for (Order child : order.getChildren()) {
                            save(child);
                        }
                    }
                    break;
                default:
                    throw new AppServiceException("订单状态出错！");
            }
            process = order.getType().name() + "_" + process;
            process = MessageFormat.format(PropertiesUtil.getPropertiesValue(process), order.getCreated().getMember().getRealName());
            orderProcess.setName(process);
            save(orderProcess);//储存一条记录
            order.setProcess(process);
            orderService.save(order);
        } catch (Exception e) {
            logger.error("保存订单流程出错{}", e.getMessage());
        }
    }
}
