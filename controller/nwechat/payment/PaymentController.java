package com.cb.controller.nwechat.payment;

import com.cb.common.core.controller.CommonController;
import com.cb.common.exception.AppServiceException;
import com.cb.common.util.StringUtil;
import com.cb.common.util.WeixinUtil;
import com.cb.service.payment.PaymentService;
import com.cb.vo.ResultVo;
import me.chanjar.weixin.mp.api.WxMpPayService;
import me.chanjar.weixin.mp.bean.pay.WxPayOrderNotifyResponse;
import me.chanjar.weixin.mp.bean.pay.result.WxPayBaseResult;
import me.chanjar.weixin.mp.bean.pay.result.WxPayOrderNotifyResult;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by GuoMIn on 2017/4/8.
 * 微信端订单控制器
 */
@Controller("nwechat-paymentController")
@RequestMapping("/nwechat/payment")
public class PaymentController extends CommonController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 请求支付
     *
     * @param codes 订单编号
     * @return 返回请求结果
     */
    @RequestMapping(value = "/order", method = RequestMethod.POST)
    @ResponseBody
    public String order(String codes) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(codes)) {
            try {
                Map<String, Object> payInfo = paymentService.pay(codes);
                result.setData(payInfo);
                result.success();
            } catch (AppServiceException e) {
                logger.error("发起微信支付出错：{}", e);
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("发起微信支付出错：{}", e);
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }


    /**
     * 微信支付回调
     */
    @RequestMapping("/notify")
    @ResponseBody
    public String payNotify() {
        WxMpPayService wxMpPayService = WeixinUtil.getWxService().getPayService();
        try {
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            WxPayOrderNotifyResult wxPayOrderNotifyResult = wxMpPayService.getOrderNotifyResult(xmlResult);
            // 结果正确
            String orderCode = wxPayOrderNotifyResult.getOutTradeNo();
            String tradeNo = wxPayOrderNotifyResult.getTransactionId();
            String totalFee = WxPayBaseResult.feeToYuan(wxPayOrderNotifyResult.getTotalFee());
            //自己处理订单的业务逻辑，需要判断订单是否已经支付过，否则可能会重复调用
            paymentService.notify(orderCode, tradeNo, totalFee);
            return WxPayOrderNotifyResponse.success("处理成功!");
        } catch (Exception e) {
            logger.error("微信回调结果异常,异常原因{}", e);
            return WxPayOrderNotifyResponse.fail(e.getMessage());
        }
    }

    /**
     * 微信支付结果查询
     *
     * @param code 订单编号
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public String query(String code) {
        ResultVo resultVo = new ResultVo();
        if (StringUtil.isNotBlank(code)){
            resultVo.setData(paymentService.result(code));
            resultVo.success();
        }else {
            resultVo.setMessage("参数不能为空");
        }
        return resultVo.toJsonString();
    }

    /**
     * 微信取消支付
     */
    @RequestMapping(value = "/cancel/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String cancel(@PathVariable String code) {
        ResultVo result = new ResultVo();
        try {
            paymentService.cancel(code);
            result.success();
        } catch (AppServiceException e) {
            logger.error("发起微信取消支付出错：{}", e);
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("发起微信取消支付出错：{}", e);
        }
        return result.toJsonString();
    }
}
