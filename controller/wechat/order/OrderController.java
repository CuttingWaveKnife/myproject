package com.cb.controller.wechat.order;

import com.cb.common.core.controller.CommonController;
import com.cb.common.exception.AppRollbackException;
import com.cb.common.exception.AppServiceException;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.JsonUtil;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.common.util.WeixinUtil;
import com.cb.model.common.ImageDatabase;
import com.cb.model.member.Member;
import com.cb.model.order.Order;
import com.cb.model.order.OrderProcess;
import com.cb.model.order.OrderProduct;
import com.cb.model.product.Product;
import com.cb.service.order.OrderService;
import com.cb.vo.order.OrderMemberVo;
import com.cb.vo.ResultVo;
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

/**
 * 微信端订单控制器
 */
@Controller("wechat-orderController")
@RequestMapping("/wechat/order")
public class OrderController extends CommonController {

    @Autowired
    private OrderService orderService;

    /**
     * 请求提交新订单
     *
     * @param order 订单
     * @return 返回提交结果
     */
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    @ResponseBody
    public String submit(Order order, String orderProducts) {
        ResultVo result = new ResultVo();
        try {
            order = orderService.create(order);
            if (order.getId() != null) {
                result.success();
                result.put("code", order.getCode());
                result.put("payAmount", order.getPayAmount());
                if (order.getUser().getMember().getAgent() == null) {
                    result.put("realName", "公司");//没有上级，直接向上级付款
                } else {
                    result.put("realName", order.getUser().getMember().getAgent().getRealName());
                }
                //订单已提交 提醒用户去付款
                WeixinUtil.sendSubmitOrderMessage(order);
            }
        } catch (AppServiceException e) {
            logger.error("提交订单出错：{}", e.getMessage());
            result.setMessage(e.getMessage());
            result.setData(JsonUtil.fromJson(e.getMessage()));
        } catch (AppRollbackException e) {
            result.setMessage("库存不足");
            result.setData(JsonUtil.fromJson(e.getMessage()));
        } catch (Exception e) {
            logger.error("提交订单出错：{}", e);
        }
        return result.toJsonString();
    }

    /**
     * 请求订单列表页面
     *
     * @return 订单列表页面
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("level", ShiroSecurityUtil.getCurrentMember().getLevel());
        model.addAttribute("wechatpay", ShiroSecurityUtil.getCurrentMember().getAgent() == null);
        return "/wechat/order/list";
    }

    /**
     * 请求根据条件分页查询订单
     *
     * @param code     订单编号
     * @param status   订单状态
     * @param pageNo   页码
     * @param pageSize 每页数据量
     * @return 返回查询结果集
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public String find(String code, String status, Integer pageNo, Integer pageSize) {
        ResultVo result = new ResultVo();
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? 10 : pageSize;
        Page<Map<String, Object>> page = new Page<>(pageNo, pageSize);
        Map<String, Object> params = new HashMap<>();
        String memberId = ShiroSecurityUtil.getCurrentMemberId();
        if (StringUtil.isNotBlank(memberId)) {
            params.put("memberId", memberId);
            if (StringUtil.isNotBlank(code)) {
                params.put("code", code);
            }
            if (StringUtil.isNotBlank(status)) {
                params.put("status", status.split(","));
            }
            params.put("imageType", ImageDatabase.TypeEnum.ORDER);
            try {
                page = orderService.findMapPageByParams(page, params);
                if (page.getResult() != null) {
                    for (Map<String, Object> map : page.getResult()) {
                        if (map.get("level") != null) {
                            map.put("level", Member.LevelEnum.valueOf((String) map.get("level")).getDesc());
                        }
                        if (map.get("status") != null) {
                            map.put("statusDesc", Order.StatusEnum.valueOf((String) map.get("status")).getDesc());
                        }
                    }
                }
                result.put("page", page);
                Member agent = ShiroSecurityUtil.getCurrentMember().getAgent();
                if (agent != null) {
                    result.put("parentName", agent.getRealName());
                    result.put("parentHead", agent.getHead());
                }
                result.success();
            } catch (Exception e) {
                logger.error("微信端查询订单列表出错{}", e);
                result.setMessage("查询订单列表出错");
            }
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }

    /**
     * 请求查看订单详情页面
     *
     * @param code  订单编号
     * @param model 数据存储模型
     * @return 返回订单详情页面
     */
    @RequestMapping(value = "/detail/{code}", method = RequestMethod.GET)
    public String detail(@PathVariable String code, Model model) {
        if (StringUtil.isNotBlank(code)) {
            Order order = orderService.findByCode(code);
            if (order != null) {
                model.addAttribute("order", order);
            }
        }
        return "/wechat/order/detail";
    }

    /**
     * 请求取消订单
     *
     * @param code 订单编号
     * @return 返回取消结果
     */
    @RequestMapping(value = "/cancel/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String cancel(@PathVariable String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            try {
                orderService.cancel(code);
                result.success();
            } catch (AppServiceException e) {
                logger.error("取消订单出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("取消订单出错{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 请求代理发货
     *
     * @param code 订单编号
     * @return 返回发货结果
     */
    @RequestMapping(value = "/deliver/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String deliver(@PathVariable String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            try {
                orderService.deliverSelf(code);
                result.success();
            } catch (AppServiceException e) {
                logger.error("取消订单出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("取消订单出错{}", e);
            }

        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 请求确认付款
     *
     * @param codes 订单编号
     * @return 返回确认结果
     */
    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    @ResponseBody
    public String receive(String codes) {//确认付款时发送上级审核提醒
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(codes)) {//合单
            try {
                orderService.receive(codes);
                result.success();
            } catch (AppServiceException e) {
                logger.error("确认付款出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("确认付款出错{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 请求确认收款
     *
     * @param code 订单编号
     * @return 返回确认结果
     */
    @RequestMapping(value = "/confirm/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String confirm(@PathVariable String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            try {
                orderService.auditByMember(code);
                result.success();
            } catch (AppServiceException e) {
                logger.error("审核订单出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("审核订单出错{}", e);
            }

        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 请求确认收货
     *
     * @param code 订单编号
     * @return 返回确认结果
     */
    @RequestMapping(value = "/receipt/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String receipt(@PathVariable String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            try {
                orderService.receipt(code);
                result.success();
            } catch (AppServiceException e) {
                logger.error("确认收货出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("确认收货出错{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    @RequestMapping(value = "/products/{code}", method = RequestMethod.GET)
    public String products(@PathVariable String code, Model model) {
        Order order = orderService.findByCode(code);
        if (order != null) {
            model.addAttribute("order", order);
        }
        return "/wechat/order/products";
    }

    /**
     * 请求再次购买
     *
     * @param code 订单编号
     * @return 返回购买产品
     */
    @RequestMapping(value = "/again/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String again(@PathVariable String code) {
        ResultVo result = new ResultVo();
        try {
            Order order = orderService.findByCode(code);
            List<Map<String, Object>> list = new ArrayList<>();
            if (order != null) {
                for (OrderProduct orderPrudct : order.getProducts()) {
                    Product product = orderPrudct.getProduct();
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", product.getCode());
                    map.put("quantity", orderPrudct.getQuantity());
                    map.put("unit", orderPrudct.getUnit());
                    map.put("unitPrice", orderPrudct.getUnitPrice());
                    list.add(map);
                }
                result.put("products", list);
                result.success();
            }
        } catch (AppServiceException e) {
            logger.error("再次购买出错：" + e.getMessage());
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("再次购买出错：" + e);
        }
        return result.toJsonString();
    }

    /**
     * 请求删除订单
     *
     * @return 返回删除结果
     */
    @RequestMapping("/delete/{code}")
    @ResponseBody
    public String delete(@PathVariable String code) {
        ResultVo result = new ResultVo();
        try {
            orderService.delete(code);
            result.success();
        } catch (AppServiceException e) {
            logger.error("再次购买出错：" + e.getMessage());
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("删除订单信息出错{}", e);
        }
        return JsonUtil.toFullJson(result);
    }

    @RequestMapping(value = "/express/{code}", method = RequestMethod.GET)
    public String express(@PathVariable String code, Model model) {
        if (StringUtil.isNotBlank(code)) {
            Order order = orderService.findByCode(code);
            model.addAttribute("order", order);
            List<OrderProcess> processes = order.getProcesses();
            if (order.getStatus().equals(Order.StatusEnum.COMPLETED)) {
                model.addAttribute("process", processes.get(0));
                processes.remove(0);
            }
            model.addAttribute("processes", order.getProcesses());
        }
        return "/wechat/order/express";
    }

    @RequestMapping(value = "/mixing", method = RequestMethod.GET)
    public String mixing() {
        return "wechat/order/mixorder";
    }

    @RequestMapping(value = "/pay", method = RequestMethod.GET)
    public String pay(Model model) {
        return "wechat/order/pay";
    }
}
