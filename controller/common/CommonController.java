package com.cb.controller.common;

import com.cb.common.cos.Credentials;
import com.cb.common.cos.Sign;
import com.cb.common.util.*;
import com.cb.model.member.Member;
import com.cb.service.member.MemberService;
import com.cb.vo.ResultVo;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.response.etms.EtmsTraceGetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 通用控制器
 */
@Controller
@RequestMapping("/common")
public class CommonController extends com.cb.common.core.controller.CommonController {

    @Autowired
    private MemberService memberService;

    /**
     * 请求发送验证
     *
     * @param mobile 手机号码
     * @return 返回发送结果
     */
    @RequestMapping(value = "/send-verification-code")
    @ResponseBody
    public String sendVerificationCode(String mobile) {
        ResultVo result = new ResultVo();
        String randNum = StringUtil.randomNumeric(4);
        String lastTime = getTime();
        if (StringUtil.isNotBlank(lastTime) && System.currentTimeMillis() - Long.valueOf(lastTime) < 2 * 60 * 1000) {
            result.put("reload", true);
            result.setMessage("两分钟内只能发送一条短信验证码");
        } else {
            // 短信发送:
            Map<String, Object> smsResult = SmsUtil.sendSmsPhoneCode(mobile, randNum);
//            Map<String, Object> smsResult = new HashMap<>();smsResult.put("code", 0);
            if (0 == (Integer) smsResult.get("code")) {
                saveMobileCode(mobile, randNum);
                result.success();
            } else {
                result.setMessage("短信发送失败");
            }
            logger.info("mobile:{},code:{}", mobile, randNum);
        }
        return JsonUtil.toFullJson(result);
    }

    /**
     * 请求检查手机验证码
     *
     * @param mobile 手机号码
     * @param code   短信验证码
     * @return 返回验证结果
     */
    @RequestMapping(value = "/check-verification-code")
    @ResponseBody
    public String checkVerificationCode(String mobile, String code) {
        Boolean result = false;
        String lastTime = getTime();
        if (StringUtil.isBlank(lastTime) || System.currentTimeMillis() - Long.valueOf(lastTime) < 15 * 60 * 1000) {
            if (StringUtil.equals(mobile, getMobile()) && StringUtil.equals(code, getCode())) {
                result = true;
            }
        }
        return result.toString();
    }

    /**
     * 请求检查手机是否存在,并查询该用户ID
     *
     * @param parentMobile 手机号码
     * @return 返回验证结果
     */
    @RequestMapping(value = "/check-phone", method = RequestMethod.POST)
    @ResponseBody
    public String checkMobile(String parentMobile) {//GuoMIn 查询关联号码是否正确，正确则查询id 暂时写这里
        boolean result = false;
        ResultVo resultVo = new ResultVo();
        if (StringUtil.isNotBlank(parentMobile)) {
            result = memberService.isExist(parentMobile);
            resultVo.setMessage("引荐人不存在");
            if (result) {
                resultVo.success();
                resultVo.setMessage("成功");
                Member member = memberService.findByMobile(parentMobile);//GuoMIn新加的
                resultVo.put("result", result);
                resultVo.put("parentId", member.getId());
            }
        } else {
            resultVo.setMessage("请输入引荐人号码");
        }
        return JsonUtil.toJson(resultVo);
    }

    /**
     * 请求检查手机是否存在
     *
     * @param mobile       手机号码
     * @param parentMobile 父级手机号码
     * @param oldMobile    原始手机号码
     * @return 返回验证结果
     */
    @RequestMapping(value = "/check-mobile")
    @ResponseBody
    public String checkMobile(String mobile, String parentMobile, String oldMobile) {
        boolean result = false;
        if (StringUtil.isNotBlank(mobile)) {
            if (StringUtil.equals(mobile, oldMobile)) {
                result = true;
            } else {
                result = !memberService.isExist(mobile);
            }
        } else if (StringUtil.isNotBlank(parentMobile)) {
            result = memberService.isExist(parentMobile);
        }
        return JsonUtil.toFullJson(result);
    }

    /**
     * 获取快递单物流跟踪信息
     *
     * @param numberCode 快递单号
     * @return
     */
    @RequestMapping(value = "/express", method = RequestMethod.POST)
    @ResponseBody
    public String express(String numberCode, String company) {// TODO: 2017/3/14 未验证
        String string = "";
        if ("京东快递".equals(company)) {
            EtmsTraceGetResponse jods = new EtmsTraceGetResponse();
            try {
                jods = JodUtils.getLogistics(numberCode);
            } catch (JdException e) {
                logger.error("京东宙斯查询出错{}", e.getMessage());
            }
            return JsonUtil.toJson(jods);
        } else {
            try {
                string = JodUtils.getKuaiDi(numberCode, company);
            } catch (Exception e) {
                logger.error("快递一百查询出错{}", e.getMessage());
            }
        }
        return string;
    }

    /**
     * 检查是否有微信授权
     *
     * @return 返回结果
     */
    @RequestMapping(value = "/check-oauth", method = RequestMethod.GET)
    @ResponseBody
    public String checkOauth() {
        ResultVo result = new ResultVo();
        result.put("result", WeixinUtil.checkOauth(request).toString());
        result.success();
        return result.toJsonString();
    }

    /**
     * 异步微信授权请求
     *
     * @param code 微信code
     * @param url  返回url
     * @param flag 标识
     * @return 返回结果
     */
    @RequestMapping(value = "/oauth", method = RequestMethod.GET)
    @ResponseBody
    public String oauth(String code, String url, boolean flag) {
        return WeixinUtil.oauth2(request, code, url, memberService, flag).toJsonString();
    }

    /**
     * 腾讯云文件上传签名算法
     *
     * @return 返回签名结果
     */
    @RequestMapping(value = "/sign-cos", method = RequestMethod.GET)
    @ResponseBody
    public String signCos(String file) {
        ResultVo result = new ResultVo();
        try {
            Credentials credentials = new Credentials(1252048288, "AKIDSs9E7phf9ZrytcEXimgOwJTRD3hCbeaK", "gBUdaLuR3WnM8j9ONBnMoPuDJ3bx62oa");
            result.put("onceSign", Sign.getOneEffectiveSign("cbmms", file, credentials));
            result.put("multiSign", Sign.getPeriodEffectiveSign("cbmms", file, credentials, System.currentTimeMillis() / 1000 + 24 * 60 * 60));
            result.success();
        } catch (Exception e) {
            logger.error("腾讯云文件上传签名算法出错:{}", e);
        }
        return result.toJsonString();
    }
}
