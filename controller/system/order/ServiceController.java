package com.cb.controller.system.order;

import com.cb.common.core.controller.CommonController;
import com.cb.common.exception.AppServiceException;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.ListUtil;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.order.Order;
import com.cb.service.order.OrderService;
import com.cb.vo.ResultVo;
import com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException;
import org.apache.shiro.authz.UnauthorizedException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 客服人员订单操作控制器
 */
@Controller("system-serviceController")
@RequestMapping("/system/service")
public class ServiceController extends CommonController {

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("status", Order.StatusEnum.values());
        return "/system/order/service/list";
    }

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
                params.put("startTime", temps[0]);
                params.put("endTime", temps[1]);
            }
        }
        if (StringUtil.isNotBlank(status)) {
            params.put("status", status.split(","));
            model.addAttribute("fettle", status);
        } else {
            status = "AUDITING,COMPLETED,RECEIVEING,FINREVOKED";
            params.put("status", status.split(","));
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
        params.put("$OrderBy", " o.creationTime desc");
        page = orderService.findPageByParams(page, params);
        model.addAttribute("page", page);
        return "/system/order/service/find";
    }

    /**
     * 请求客服审核订单
     *
     * @param code 订单编号
     * @return 返回审核结果
     */
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    @ResponseBody
    public String audit(BigDecimal amount, String remark, String code, String account, String source, String[] codes, String[] warehouses) {//客服审核
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code) && amount != null) {
            try {
                orderService.auditByService(code, codes, warehouses, amount, remark, account, source);
                result.success();
            } catch (AppServiceException e) {
                logger.error("客服审核订单出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (UnauthorizedException e) {
                logger.error("客服审核订单没有权限{}", e.getMessage());
                result.setMessage("没有权限");
            } catch (Exception e) {
                logger.error("客服审核订单出错{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 客服，财务取消订单
     *
     * @return
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public String cancel(String remark, String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            try {
                orderService.revoke(code, remark);
                result.success();
            } catch (AppServiceException e) {
                logger.error("取消订单出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (PersistenceException e) {
                logger.error("取消订单操作太频繁出错{}", e.getMessage());
                result.setMessage("操作太快啦！");
            } catch (Exception e) {
                logger.error("取消订单出错{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }


}
