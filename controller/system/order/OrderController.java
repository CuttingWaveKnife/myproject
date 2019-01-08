package com.cb.controller.system.order;


import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.JsonUtil;
import com.cb.common.util.ListUtil;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.order.Order;
import com.cb.service.order.OrderService;
import com.cb.service.warehouse.WarehouseService;
import com.cb.vo.ResultVo;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("system-orderController")
@RequestMapping("/system/order")
public class OrderController extends CommonController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 撤单列表页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/cancel/list", method = RequestMethod.GET)
    public String cancellist(Model model) {
        model.addAttribute("status", Order.StatusEnum.values());
        return "/system/order/cancel/list";
    }

    /**
     * 撤单列表数据
     *
     * @return
     */
    @RequestMapping(value = "/cancel/find", method = RequestMethod.GET)
    public String cancelfind(Order.TypeEnum type, String search, String orderBy, String amount, String datepicker, String status, Integer pageNo, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<Order> page = new Page<>(pageNo, 20);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("code", search);
            params.put("name", search);
        }
        if (StringUtil.isNotBlank(amount)) {
            params.put("amount", amount);
        }
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("startTime", temps[0] + " 00:00:00");
                params.put("endTime", temps[1] + " 23:59:59");
            }
        }
        if (StringUtil.isNotBlank(status)) {
            params.put("status", status.split(","));
            model.addAttribute("fettle", status);
        } else {
            status = "CANNOT,FINCANCELED,CUSCANCELED";
            params.put("status", status.split(","));
        }
        params.put("type", Order.TypeEnum.SYSTEM);
        boolean[] booleans = ShiroSecurityUtil.getSubject().hasRoles(ListUtil.arrayToList(new String[]{"super", "management", "finance"}));
        for (boolean aBoolean : booleans) {
            if (aBoolean) {
                params.remove("userId");
                params.remove("auditUserId");
            }
        }
        if ("createTime".equals(orderBy)) {//下单时间排序
            params.put("$OrderBy", " o.creationTime desc");
        } else {//审核时间排序
            params.put("$OrderBy", " o.auditDate desc");
        }
        page = orderService.findPageByParams(page, params);
        model.addAttribute("page", page);
        return "/system/order/cancel/find";
    }

    /**
     * 订单管理 - 查看订单页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("status", Order.StatusEnum.values());
        return "/system/order/list";
    }

    /**
     * 订单管理 - 查看订单数据页面
     *
     * @return
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String find(Order.TypeEnum type, String search, String orderBy, String amount, String datepicker, String status, Integer pageNo, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<Order> page = new Page<>(pageNo, 20);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("code", search);
            params.put("name", search);
        }
        if (StringUtil.isNotBlank(amount)) {
            params.put("amount", amount);
        }
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("startTime", temps[0] + " 00:00:00");
                params.put("endTime", temps[1] + " 23:59:59");
            }
        }
        if (StringUtil.isNotBlank(status)) {
            params.put("status", status.split(","));
            model.addAttribute("fettle", status);
        }

        if (Order.TypeEnum.SYSTEM.equals(type)) {
            params.put("userId", ShiroSecurityUtil.getCurrentUserId());//后台审核
        } else {
            params.put("auditUserId", ShiroSecurityUtil.getCurrentUserId());
        }
        params.put("type", type);
        boolean[] booleans = ShiroSecurityUtil.getSubject().hasRoles(ListUtil.arrayToList(new String[]{"super", "management", "finance"}));
        for (boolean aBoolean : booleans) {
            if (aBoolean) {
                params.remove("userId");
                params.remove("auditUserId");
            }
        }
        if ("createTime".equals(orderBy)) {//下单时间排序
            params.put("$OrderBy", " o.creationTime desc");
        } else {//审核时间排序
            params.put("$OrderBy", " o.auditDate desc");
        }
        page = orderService.findPageByParams(page, params);
        model.addAttribute("page", page);
        return "/system/order/find";
    }

    /**
     * 获取一个合并订单下所有单独订单页面
     */
    @RequestMapping(value = "/get/{code}", method = RequestMethod.GET)
    public String get(@PathVariable String code, Model model) {
        List<Order> orders = new ArrayList<>();
        Order order = orderService.findByCode(code);
        if (order != null) {
            for (Order child : order.getChildren()) {
                if (child.getType().equals(Order.TypeEnum.ALONE)) {
                    orders.add(child);//单独订单
                } else {//合单再拆
                    for (Order c : child.getChildren()) {
                        if (c.getType().equals(Order.TypeEnum.ALONE)) {
                            orders.add(c);
                        } else {
                            for (Order oc : c.getChildren()) {
                                if (oc.getType().equals(Order.TypeEnum.ALONE)) {
                                    orders.add(oc);
                                }
                            }
                        }
                    }
                }
            }
            model.addAttribute("payAmount", order.getPayAmount());
        }
        model.addAttribute("code", code);
        model.addAttribute("orders", orders);
        model.addAttribute("warehouses", warehouseService.findAll());
        return "/system/order/get";
    }

    /**
     * 获取后台订单详情
     */
    @RequestMapping(value = "/detail/{code}", method = RequestMethod.GET)
    public String detail(@PathVariable String code, Model model) {
        List<Order> orders = new ArrayList<>();
        if (StringUtil.isNotBlank(code)) {
            Order order = orderService.findByCode(code);
            model.addAttribute("order", order);
            if (order != null) {
                for (Order child : order.getChildren()) {
                    if (child.getType().equals(Order.TypeEnum.ALONE)) {
                        orders.add(child);//单独订单
                    } else {//合单再拆
                        for (Order c : child.getChildren()) {
                            if (c.getType().equals(Order.TypeEnum.ALONE)) {
                                orders.add(c);
                            } else {
                                for (Order oc : c.getChildren()) {
                                    if (oc.getType().equals(Order.TypeEnum.ALONE)) {
                                        orders.add(oc);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        model.addAttribute("orders", orders);
        return "/system/order/detail";
    }

    /**
     * 恢复取消的订单 系统单更改为审核中 该单所有子订单依旧是取消状态，不改变
     * // TODO: 2017/3/15 未启用，先留着
     *
     * @param code
     * @return
     */
    @RequestMapping(value = "/restore/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String restore(@PathVariable String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            Order order = orderService.findByCode(code);
            if (order != null) {
                order.setStatus(Order.StatusEnum.AUDITING);
                orderService.save(order);
                result.success();
            } else {
                result.setMessage("订单不存在");
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 请求删除订单
     *
     * @param ids 所有需要删除的订单id
     * @return 返回删除结果
     */
    @RequestMapping("/delete")
    @ResponseBody
    public String delete(String ids) {
        ResultVo result = new ResultVo();
        try {
            ShiroSecurityUtil.getSubject().checkRole("super");  //判断是否有是超级管理员
            if (StringUtil.isNotBlank(ids)) {
                int num = orderService.batchDelete(ids.split(","));
                result.success();
                result.setMessage("成功删除" + num + "条订单信息");
                logger.warn("用户{}删除订单{}，此操作共影响数据库{}条数据！", ShiroSecurityUtil.getCurrentUserId(), ids, num);
            } else {
                result.setMessage("参数不能为空");
            }
        } catch (UnauthorizedException e) {
            result.setMessage("没有删除权限");
        } catch (Exception e) {
            logger.error("删除订单信息出错{}", e);
        }
        return JsonUtil.toFullJson(result);
    }

    /**
     * 请求删除订单
     *
     * @param ids 所有需要删除的订单id
     * @return 返回删除结果
     */
    @RequestMapping("/cancel/{code}")
    @ResponseBody
    public String cancel(@PathVariable String code) {
        ResultVo result = new ResultVo();
        try {
            ShiroSecurityUtil.getSubject().checkRole("super");  //判断是否有是超级管理员
            if (StringUtil.isNotBlank(code)) {
                orderService.cancel(code);
                result.success();
                logger.warn("用户{}取消订单{}！", ShiroSecurityUtil.getCurrentUserId(), code);
            } else {
                result.setMessage("参数不能为空");
            }
        } catch (UnauthorizedException e) {
            result.setMessage("没有取消权限");
        } catch (Exception e) {
            logger.error("取消订单信息出错{}", e);
        }
        return JsonUtil.toFullJson(result);
    }
}
