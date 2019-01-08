package com.cb.controller.system.payment;

import com.cb.common.core.controller.CommonController;
import com.cb.common.exception.AppServiceException;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DateUtil;
import com.cb.common.util.ExclUtil;
import com.cb.common.util.PropertiesUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.order.Order;
import com.cb.model.payment.PaymentRecord;
import com.cb.service.payment.PaymentRecordService;
import com.cb.service.payment.PaymentService;
import com.cb.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信端订单控制器
 */
@Controller("system-paymentController")
@RequestMapping("/system/payment")
public class PaymentController extends CommonController {

    @Autowired
    private PaymentRecordService paymentRecordService;

    @Autowired
    private PaymentService paymentService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("type", PaymentRecord.TypeEnum.values());
        model.addAttribute("status", PaymentRecord.StatusEnum.values());
        return "/system/payment/list";
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String find(String search, PaymentRecord.TypeEnum type, PaymentRecord.StatusEnum status, String datepicker, Integer pageNo, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<PaymentRecord> page = new Page<>(pageNo, 20);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("search", search);
        }
        if (type != null) {
            params.put("type", type);
        }
        if (status != null) {
            params.put("status", status);
        }
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("startTime", temps[0]);
                params.put("endTime", temps[1]);
            }
        }
        model.addAttribute("page", paymentRecordService.findPageByParams(page, params));
        return "/system/payment/find";
    }

    @RequestMapping(value = "/refund/list", method = RequestMethod.GET)
    public String refundList(Model model) {
        model.addAttribute("type", PaymentRecord.TypeEnum.values());
        model.addAttribute("status", PaymentRecord.StatusEnum.values());
        return "/system/payment/refund-list";
    }

    @RequestMapping(value = "/refund/find", method = RequestMethod.GET)
    public String refundFind(String search, PaymentRecord.TypeEnum type, String datepicker, Integer pageNo, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<PaymentRecord> page = new Page<>(pageNo, 20);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("search", search);
        }
        if (type != null) {
            params.put("type", type);
        }
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("startTime", temps[0]);
                params.put("endTime", temps[1]);
            }
        }
        params.put("status", PaymentRecord.StatusEnum.REFUND);
        model.addAttribute("page", paymentRecordService.findPageByParams(page, params));
        return "/system/payment/refund-find";
    }

    /**
     * 微信取消支付
     */
    @RequestMapping(value = "/cancel/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String cancel(@PathVariable String code) {
        ResultVo result = new ResultVo();
        try {
            paymentService.cancelBySystem(code);
            result.success();
        } catch (AppServiceException e) {
            logger.error("管理系统微信取消支付出错：{}", e);
            result.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("管理系统微信取消支付出错：{}", e);
        }
        return result.toJsonString();
    }

    /**
     * 导出财务订单明细
     *
     * @param datepicker
     * @return
     */
    @RequestMapping(value = "/exceloutPayment", method = RequestMethod.POST)
    @ResponseBody
    public String exceloutPayment(String datepicker, String status) {
        ResultVo resultVo = new ResultVo();
        Map<String, Object> paymentParams = new HashMap<>();
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                paymentParams.put("modificationStartTime", temps[0] + " 00:00:00");
                paymentParams.put("modificationEndTime", temps[1] + " 23:59:59");
            }
        } else {
            paymentParams.put("modificationStartTime", DateUtil.dateToString(DateUtil.newInstanceDateBegin(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 00:00:00");
            paymentParams.put("modificationEndTime", DateUtil.dateToString(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 23:59:59");
        }

        String paymentTable = PropertiesUtil.getPropertiesValue("uploadurl") + "支付流水.xls";
        File paymentFile = new File(paymentTable);
        if (paymentFile.exists()) {
            paymentFile.delete();
        }
        String paymentUrl = PropertiesUtil.getPropertiesValue("downurl") + "支付流水.xls";
        paymentParams.put("status",new String[]{"REFUND","SUCCESS"});
        List<Map<String, Object>> paymentList =  paymentRecordService.findPageByParamsStatus(paymentParams);

        try {
            ExclUtil.excelOutPayment(paymentList, paymentTable);//导出流水
            resultVo.put("url", paymentUrl);
            resultVo.success();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultVo.toJsonString();
    }
}
