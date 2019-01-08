package com.cb.controller.system.order;

import com.cb.common.core.controller.CommonController;
import com.cb.common.exception.AppServiceException;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.*;
import com.cb.model.member.Member;
import com.cb.model.order.Order;
import com.cb.service.member.MemberService;
import com.cb.service.order.OrderService;
import com.cb.service.payment.PaymentRecordService;
import com.cb.vo.ResultVo;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.*;

/**
 * 财务操作订单控制器
 */
@Controller("system-financeController")
@RequestMapping("/system/finance")
public class FinanceController extends CommonController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PaymentRecordService paymentRecordService;

    /**
     * 请求审核订单
     *
     * @param code 订单编号
     * @return 返回审核结果
     */
    @RequestMapping(value = "/audit/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String audit(@PathVariable String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            try {
                orderService.auditByFinance(code);
                result.success();
            } catch (AppServiceException e) {
                logger.error("审核订单出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (UnauthorizedException e) {
                logger.error("审核订单没有权限{}", e.getMessage());
                result.setMessage("审核订单没有权限");
            } catch (Exception e) {
                logger.error("审核订单出错{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 请求审核订单
     *
     * @param code 订单编号
     * @return 返回审核结果
     */
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    @ResponseBody
    public String confirmfinance(String code) {//财务批量审核
        ResultVo result = new ResultVo();
        int count = 0;
        List<String> list = new ArrayList<>();
        if (StringUtil.isNotBlank(code)) {
            String[] codes = code.split(",");
            for (String c : codes) {
                if (StringUtil.isNotBlank(c)) {
                    try {
                        orderService.auditByFinance(c);
                        count++;
                    } catch (AppServiceException e) {
                        logger.error("审核订单出错{}", e.getMessage());
                        result.setMessage(e.getMessage());
                    } catch (UnauthorizedException e) {
                        logger.error("审核订单没有权限{}", e.getMessage());
                        list.add("审核订单没有权限");
                        result.setMessage("审核订单没有权限");
                    } catch (Exception e) {
                        logger.error("审核订单出错{}", e);
                    }
                }
            }
            result.success();
        } else {
            result.setMessage("参数不能为空");
        }
        result.put("count", count);
        result.put("list", list);
        return result.toJsonString();
    }

    /**
     * 财务审核回退给客服
     *
     * @return
     */
    @RequestMapping(value = "/revoke", method = RequestMethod.POST)
    @ResponseBody
    public String review(String id, String remark) {
        ResultVo resultVo = new ResultVo();
        if (StringUtil.isNotBlank(id)) {
            try {
                orderService.revokeOrder(id, remark);
                resultVo.success();
            } catch (Exception e) {
                logger.error("回退订单出错{}", e.getMessage());
            }
        } else {
            resultVo.setMessage("参数不能为空");
        }
        return resultVo.toJsonString();
    }

    /**
     * 进入财务审核
     *
     * @return
     */
    @RequestMapping(value = "/verify/list", method = RequestMethod.GET)
    public String verifylist(Model model, String time) {
        Map<String, Object> map = new HashMap<>();
        model.addAttribute("status", Order.StatusEnum.values());
        model.addAttribute("daterangepicker", checkTime(time, map));
        return "/system/order/finance/verifylist";
    }

    /**
     * 财务审核
     *
     * @return
     */
    @RequestMapping(value = "/verify/find", method = RequestMethod.GET)
    public String verifyfind(Order.TypeEnum type, String search, String orderBy, String amount, String datepicker, String status, Integer pageNo, Model model) {
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
                params.put("financeAuditStartTime", temps[0] + " 00:00:00");
                params.put("financeAuditEndTime", temps[1] + " 23:59:59");
            }
        }
        if (StringUtil.isNotBlank(status)) {
            params.put("status", status.split(","));
        }else {
            params.put("status", new String[]{"COMPLETED","AUDITING"});
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
        params.put("$OrderBy", " o.financeDate desc");
        page = orderService.findPageByParams(page, params);
        model.addAttribute("page", page);
        return "/system/order/finance/verifyfind";
    }

    /**
     * 进入汇总数据
     *
     * @return
     */
    @RequestMapping(value = "/summary/list", method = RequestMethod.GET)
    public String summarylist(Model model, String time) {
        Map<String, Object> params = new HashMap<>();
        model.addAttribute("levels", Member.LevelEnum.values());
        model.addAttribute("time", time);
        model.addAttribute("daterangepicker", checkTime(time, params));
        return "/system/order/finance/summarylist";
    }

    /**
     * 会员数据汇总
     */
    @RequestMapping(value = "/summary/find", method = RequestMethod.GET)
    public String find(String level, String search, String amount, String datepicker, Integer pageNo, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<Map<String, Object>> page = new Page<>(pageNo, 20);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("code", "%"+search+"%");
            params.put("name", "%"+search+"%");
        }
        if (StringUtil.isNotBlank(amount)) {
            params.put("amount", amount);
        }
        if (StringUtil.isNotBlank(level)) {
            params.put("level", level);
        }
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("financeAuditStartTime", temps[0] + " 00:00:00");
                params.put("financeAuditEndTime", temps[1] + " 23:59:59");
            }
        }
        params.put("status", new String[]{"COMPLETED"});
        params.put("type", Order.TypeEnum.SYSTEM);
        params.put("$OrderBy", " o.financeDate desc");
        params.put("$groupBy", " oo.created_id");//按会员分类
        model.addAttribute("datepicker", datepicker);
        page = orderService.findPageByMember(page, params);
        Map<String, Object> map = orderService.findMapByMemberSum(params);
        model.addAttribute("page", page);
        model.addAttribute("map", map);
        return "/system/order/finance/summaryfind";
    }

    /**
     * 会员数据汇总详情
     */
    @RequestMapping(value = "/summary/details", method = RequestMethod.GET)
    public String detail(String mobile, Model model, Integer pageNo, String datepicker) {
        Member member = memberService.findByMobile(mobile);
        pageNo = pageNo == null ? 1 : pageNo;
        Page<Order> page = new Page<>(pageNo, 20);
        Map<String, Object> params = new HashMap<>();
        if (member != null) {
            params.put("memberId", member.getId());
            params.put("type", Order.TypeEnum.SYSTEM);
            params.put("status", new String[]{"COMPLETED"});
            params.put("$OrderBy", " o.financeDate desc");
            if (StringUtil.isNotBlank(datepicker)) {
                String[] temps = datepicker.split(" - ");
                if (temps.length == 2) {
                    params.put("auditStartTime", temps[0] + " 00:00:00");
                    params.put("auditEndTime", temps[1] + " 23:59:59");
                }
            }
            page = orderService.findPageByMemberDetails(page, params);
            model.addAttribute("datepicker", datepicker);
            model.addAttribute("page", page);
        }
        return "system/order/finance/summarydetails";
    }

    /**
     * 导出财务订单明细
     *
     * @param datepicker
     * @return
     */
    @RequestMapping(value = "/exceloutfinance", method = RequestMethod.POST)
    @ResponseBody
    public String exceloutFinance(String datepicker, String status) {
        ResultVo resultVo = new ResultVo();
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("financeAuditStartTime", temps[0] + " 00:00:00");
                params.put("financeAuditEndTime", temps[1] + " 23:59:59");
            }
        } else {
            params.put("financeAuditStartTime", DateUtil.dateToString(DateUtil.newInstanceDateBegin(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 00:00:00");
            params.put("financeAuditEndTime", DateUtil.dateToString(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 23:59:59");
        }
        params.put("status", new String[]{"COMPLETED"});//统计通过了客服审核的系统单
        params.put("type", Order.TypeEnum.SYSTEM);
        params.put("$OrderBy", " o.finance_date desc");
        List<Map<String, Object>> list = orderService.findMapByParamsAlone(params);
        String financeTable = PropertiesUtil.getPropertiesValue("uploadurl") + "orderfinance.xls";
        File financeFile = new File(financeTable);
        if (financeFile.exists()) {
            financeFile.delete();
        }
        String financeUrl = PropertiesUtil.getPropertiesValue("downurl") + "orderfinance.xls";
        Map<String, Object> sumMap = orderService.findMapByMemberSum(params);

        try {
            ExclUtil.excelOutFinanceDay(list, financeTable, orderService, sumMap);//导出财务
            resultVo.put("url", financeUrl);
            resultVo.success();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultVo.toJsonString();
    }

    /**
     * 导出财务明细 按会员 每天 分类 导出下单信息 类型 完成单
     *
     * @return
     */
    @RequestMapping(value = "/excelout-summary-day", method = RequestMethod.POST)
    @ResponseBody
    public String exceloutSummary(String datepicker, String status, String groupBy, Integer pageNo, Integer pageSize) {
        ResultVo resultVo = new ResultVo();
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = -1;
        Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageNo, pageSize);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("financeAuditStartTime", temps[0] + " 00:00:00");
                params.put("financeAuditEndTime", temps[1] + " 23:59:59");
            }
        } else {
            params.put("financeAuditStartTime", DateUtil.dateToString(DateUtil.newInstanceDateBegin(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 00:00:00");
            params.put("financeAuditEndTime", DateUtil.dateToString(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 23:59:59");
        }
        params.put("type", Order.TypeEnum.SYSTEM);
            params.put("status", new String[]{"COMPLETED"});//审核中，已完成 只统计确认收款的单
        params.put("$groupBy", " oo.created_id,dateTime");
//        String df = session.getServletContext().getRealPath("/") + "static\\xsl\\数据明细.xls";
        String df = PropertiesUtil.getPropertiesValue("uploadurl") + "数据明细.xls";
        File file = new File(df);
        if (file.exists()) {
            file.delete();
        }
        String dd = PropertiesUtil.getPropertiesValue("downurl") + "数据明细.xls";
        Page<Map<String, Object>> list = orderService.findPageByMember(page, params);//查询单的类型是系统单
        Map<String, Object> sumMap = orderService.findMapByMemberSum(params);
        try {
            ExclUtil.excelOutSummary(list, df, sumMap);
            resultVo.put("url", dd);
            resultVo.success();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("取消订单出错{}", e.getMessage());
        }
        return resultVo.toJsonString();
    }

    /**
     * 导出财务汇总 按会员  分类 导出下单信息 类型 完成单
     *
     * @return
     */
    @RequestMapping(value = "/excelout-summary-member", method = RequestMethod.POST)
    @ResponseBody
    public String exceloutSummaryMember(String datepicker, String status, Integer pageNo, Integer pageSize) {
        ResultVo resultVo = new ResultVo();
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = -1;
        Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageNo, pageSize);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("financeAuditStartTime", temps[0] + " 00:00:00");
                params.put("financeAuditEndTime", temps[1] + " 23:59:59");
            }
        } else {
            params.put("financeAuditStartTime", DateUtil.dateToString(DateUtil.newInstanceDateBegin(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 00:00:00");
            params.put("financeAuditEndTime", DateUtil.dateToString(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 23:59:59");
        }
        params.put("type", Order.TypeEnum.SYSTEM);
            params.put("status", new String[]{"COMPLETED"});//审核中，已完成 只统计确认收款的单
        params.put("$groupBy", " oo.created_id");
//        String df = session.getServletContext().getRealPath("/") + "static\\xsl\\数据汇总.xls";
        String df = PropertiesUtil.getPropertiesValue("uploadurl") + "数据汇总.xls";
        File file = new File(df);
        if (file.exists()) {
            file.delete();
        }
//        String dd = request.getContextPath() + "/static/xsl/数据汇总.xls";
        String dd = PropertiesUtil.getPropertiesValue("downurl") + "数据汇总.xls";
        Page<Map<String, Object>> list = orderService.findPageByMember(page, params);//查询单的类型是系统单
        Map<String, Object> sumMap = orderService.findMapByMemberSum(params);
        try {
            ExclUtil.excelOutSummary(list, df, sumMap);
            resultVo.put("url", dd);
            resultVo.success();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("取消订单出错{}", e.getMessage());
        }
        return resultVo.toJsonString();
    }


    public String checkTime(String daterangepicker, Map<String, Object> params) {
        if (StringUtil.isBlank(daterangepicker)) {
            daterangepicker = "day";
        }
        Date start = null;
        Date end = null;
        switch (daterangepicker) {
            case "day":
                start = DateUtil.newInstanceDateBegin();
                end = DateUtil.newInstanceDateEnd();
                break;
            case "week":
                start = DateUtil.firstDayOfWeek();
                end = DateUtil.lastDayOfWeek();
                break;
            case "month":
                start = DateUtil.firstDayOfMonth();
                end = DateUtil.lastDayOfMonth();
                break;
            default:
                String[] dates = daterangepicker.split(" - ");
                if (dates.length == 2) {
                    start = DateUtil.stringToDate(dates[0], DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
                    end = DateUtil.stringToDate(dates[1], DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
                }
        }
        if (StringUtil.isNotBlank(start) && StringUtil.isNotBlank(end)) {
            String startStr = DateUtil.dateToString(start, DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
            String endStr = DateUtil.dateToString(end, DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
            params.put("auditStartTime", startStr + " 00:00:00");
            params.put("auditEndTime", endStr + " 23:59:59");
            daterangepicker = startStr + " - " + endStr;
        }
        return daterangepicker;
    }

}
