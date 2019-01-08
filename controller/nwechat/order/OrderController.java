package com.cb.controller.nwechat.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
import com.cb.vo.PageVo;
import com.cb.vo.ResultVo;
import com.cb.vo.order.OrderExpressVo;
import com.cb.vo.order.OrderMemberVo;
import com.cb.vo.order.OrderProductVo;
import com.cb.vo.order.OrderVo;

/**
 * Created by GuoMIn on 2017/4/7.
 */
@Controller("nwechat-orderController")
@RequestMapping("/nwechat/order")
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
    public String submit(Order order) {
        ResultVo result = new ResultVo();
        OrderMemberVo orderMemberVo = new OrderMemberVo();
        try {
            order = orderService.create(order);
            if (order.getId() != null) {
                orderMemberVo.setCode(order.getCode());
                orderMemberVo.setPayAmount(order.getPayAmount());
                if (order.getUser().getMember().getAgent() == null) {
                    orderMemberVo.setName("公司");
                } else {
                    orderMemberVo.setName(order.getUser().getMember().getAgent().getRealName());
                }
                //订单已提交 提醒用户去付款
                WeixinUtil.sendSubmitOrderMessage(order);
            }
            result.put("data", orderMemberVo);
            result.success();
        } catch (AppServiceException e) {
            logger.error("提交订单出错：{}", e.getMessage());
            result.setMessage(e.getMessage());
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
    @ResponseBody
    public String list() {
        ResultVo resultVo = new ResultVo();
        resultVo.put("level", ShiroSecurityUtil.getCurrentMember().getLevel());
        resultVo.put("wechatPay", ShiroSecurityUtil.getCurrentMember().getAgent() == null);
        resultVo.success();
        return resultVo.toJsonString();
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
    @RequestMapping(value = "/find", method = RequestMethod.GET,produces = "application/json; charset=utf-8")
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
                PageVo<OrderMemberVo> orderMemberVoPage = OrderMemberVo.toVoPage(page);
                result.put("orderVoPage", orderMemberVoPage);
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
     * @return 返回订单详情页面
     */
    @RequestMapping(value = "/detail/{code}", method = RequestMethod.GET,produces = "application/json; charset=utf-8")
    @ResponseBody
    public String detail(@PathVariable String code) {
        ResultVo resultVo = new ResultVo();
        OrderVo orderVo = new OrderVo();
        if (StringUtil.isNotBlank(code)) {
            Order order = orderService.findByCode(code);
            orderVo = OrderVo.toOrderVo(order); 
            resultVo.put("order", orderVo);
        }
        resultVo.success();
        return resultVo.toJsonString();
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

    /**
     * 停用
     * @param code
     * @return
     */
    @RequestMapping(value = "/products/{code}", method = RequestMethod.GET)
    @ResponseBody
    public String products(@PathVariable String code) {
        ResultVo resultVo = new ResultVo();
        Order order = orderService.findByCode(code);
        if (order != null) {
            resultVo.put("data", order);
            resultVo.success();
        }
        return resultVo.toJsonString();
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
            List<OrderProductVo> voList = new ArrayList<>();
            if (order != null) {
                for (OrderProduct orderPrudct : order.getProducts()) {
                    Product product = orderPrudct.getProduct();
                    OrderProductVo orderProductVo = new OrderProductVo();
                    orderProductVo.setCode(product.getCode());
                    orderProductVo.setQuantity(orderPrudct.getQuantity());
                    orderProductVo.setUnit(orderPrudct.getUnit());
                    orderProductVo.setUnitPrice(orderPrudct.getUnitPrice());
                    voList.add(orderProductVo);
                }
                result.put("voList", voList);
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
            logger.error("删除订单信息出错：" + e.getMessage());
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("删除订单信息出错{}", e);
        }
        return JsonUtil.toFullJson(result);
    }

    @RequestMapping(value = "/express/{code}", method = RequestMethod.GET)
    @ResponseBody
    public String express(@PathVariable String code) {
        ResultVo resultVo = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            Order order = orderService.findByCode(code);
            OrderExpressVo orderExpressVo = new OrderExpressVo();
            orderExpressVo.setStatus(order.getStatus().name());
            orderExpressVo.setExpress(order.getExpress());
            orderExpressVo.setExpressNumber(order.getExpressNumber());
            List<OrderProcess> processes = order.getProcesses();
            if (order.getStatus().equals(Order.StatusEnum.COMPLETED)) {
                orderExpressVo.setProcessName(processes.get(0).getName());
                orderExpressVo.setProcessTime(processes.get(0).getCreationTime().getTime());
                processes.remove(0);
            }
            orderExpressVo.setProcessVoList(order.getProcesses());
            resultVo.put("data", orderExpressVo);
            resultVo.success();
        }
        return resultVo.toJsonString();
    }

}
