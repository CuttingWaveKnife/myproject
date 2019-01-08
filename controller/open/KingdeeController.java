package com.cb.controller.open;


import com.cb.common.core.controller.CommonController;
import com.cb.common.exception.AppServiceException;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.Constants;
import com.cb.common.util.PropertiesUtil;
import com.cb.common.util.StringUtil;
import com.cb.common.util.WeixinUtil;
import com.cb.model.order.Order;
import com.cb.model.security.User;
import com.cb.service.order.OrderService;
import com.cb.service.security.UserService;
import com.cb.vo.ResultVo;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开放金蝶接口控制器
 */
@Controller
@RequestMapping("/open/kingdee")
public class KingdeeController extends CommonController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * 请求获取待发货的订单列表
     *
     * @param id   认证id
     * @param key  认证key
     * @param num  分页页码
     * @param size 分页数量
     * @return 返回封装json
     */
    @RequestMapping(value = "/order/list", method = RequestMethod.POST)
    @ResponseBody
    public String getOrderList(String id, String key, Integer num, Integer size) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(id) && StringUtil.isNotBlank(key)) {
            num = num == null ? 1 : num;
            size = size == null ? 10 : size;
            Page<Map<String, Object>> page = new Page<>(num, size);
            Map<String, Object> params = new HashMap<>();
            try {
                page = orderService.getOrderList(id, key, page, params);
                result.success();
                result.put("page", page);
            } catch (AppServiceException e) {
                logger.error("请求金蝶取待发货的订单列表接口出错{},id:{},key:{}", e.getMessage(), id, key);
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("请求金蝶取待发货的订单列表接口出错{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * e助下发回执
     *
     * @param id   认证id
     * @param key  认证key
     * @param code 订单编号
     * @return 返回结果
     */
    @RequestMapping(value = "/order/success", method = RequestMethod.POST)
    @ResponseBody
    public String success(String id, String key, String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(id) && StringUtil.isNotBlank(key) && StringUtil.isNotBlank(code)) {
            User user = userService.getUserByUsername(id);
            if (user != null && user.getType().equals(User.TypeEnum.OPEN)) {
                key = new SimpleHash(Constants.MD5, key, user.getSalt()).toString();
                if (key.equals(user.getPassword())) {
                    Order order = orderService.findByCode(code);
                    if (order != null) {
                        logger.info("=============收到e助回执================");
                        result.success();
                    } else {
                        result.setMessage("订单不存在");
                    }
                } else {
                    result.setMessage("没有权限");
                }
            } else {
                result.setMessage("没有权限");
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 请求确认发货
     *
     * @param id      认证id
     * @param key     认证key
     * @param code    订单编号
     * @param express 快递公司
     * @param number  快递单号
     * @return 返回审核结果
     */
    @RequestMapping(value = "/order/deliver", method = RequestMethod.POST)
    @ResponseBody
    public String orderDeliver(String id, String key, String code, String express, String number) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(id) && StringUtil.isNotBlank(key) && StringUtil.isNotBlank(code) && StringUtil.isNotBlank(express) && StringUtil.isNotBlank(number)) {
            try {
                orderService.deliver(id, key, code, express, number);
                result.success();
                List<WxMpTemplateData> wxMpTemplateMessage = new ArrayList<>();
                WxMpTemplateData first = new WxMpTemplateData("first", StringUtil.replaceMessage(PropertiesUtil.getPropertiesValue(Constants.CONFIRM_DELIVERY_TEMPLATE_FIRST), express));
                WxMpTemplateData keyword1 = new WxMpTemplateData("keyword1", code);//订单编号
                WxMpTemplateData keyword2 = new WxMpTemplateData("keyword2", express);//公司
                WxMpTemplateData keyword3 = new WxMpTemplateData("keyword3", number);//快递单号
                WxMpTemplateData remark = new WxMpTemplateData("remark", PropertiesUtil.getPropertiesValue(Constants.CONFIRM_DELIVERY_TEMPLATE_REMARK));
                wxMpTemplateMessage.add(keyword1);
                wxMpTemplateMessage.add(keyword2);
                wxMpTemplateMessage.add(keyword3);
                wxMpTemplateMessage.add(remark);
                wxMpTemplateMessage.add(first);
                WeixinUtil.sendMessage(orderService.findByCode(code).getUser().getMember().getOpenId(), PropertiesUtil.getPropertiesValue(Constants.CONFIRM_DELIVERY_TEMPLATE_ID), Constants.CONFIRM_DELIVERY_TEMPLATE_URL + code, wxMpTemplateMessage);
            } catch (AppServiceException e) {
                logger.error("请求金蝶确认发货接口出错{},id:{},key:{},code:{},express{},number:{}", e.getMessage(), id, key, code, express, number);
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("请求金蝶确认发货接口出错{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }
}
