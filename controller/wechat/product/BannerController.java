package com.cb.controller.wechat.product;

import com.cb.common.core.controller.CommonController;
import com.cb.common.util.*;
import com.cb.model.active.Record;
import com.cb.model.member.Member;
import com.cb.service.activition.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by GuoMIn on 2017/3/23.
 */
@Controller("wechat-bannerController")
@RequestMapping("/wechat/banner")
public class BannerController extends CommonController {

    @Autowired
    private RecordService recordService;

    /**
     * 活动集赞
     *
     * @return
     */
    @RequestMapping(value = "/lipstick", method = RequestMethod.GET)
    public String activition(Model model) {
        Member member = ShiroSecurityUtil.getCurrentMember();
        Record record = recordService.isExist(member.getId());
        if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {//参与了活动
            model.addAttribute("limitNumber", record.getNumber());
        } else {
            model.addAttribute("limitNumber", -1);
        }
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                ) {
            model.addAttribute("status", true);//在活动期间
        } else {
            model.addAttribute("status", false);
        }
        model.addAttribute("endTime", DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS).getTime());
        model.addAttribute("record", record);
        model.addAttribute("member", member);
        return "wechat/banner/lipstick";
    }

    /**
     * 活动集赞
     *
     * @return
     */
    @RequestMapping(value = "/lipstick-back", method = RequestMethod.GET)
    public String activition2(Model model) {
        Member member = ShiroSecurityUtil.getCurrentMember();
        Record record = recordService.isExist(member.getId());
        if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {//参与了活动
            model.addAttribute("limitNumber", record.getNumber());
        } else {
            model.addAttribute("limitNumber", -1);
        }
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                ) {
            model.addAttribute("status", true);//在活动期间
        } else {
            model.addAttribute("status", false);
        }
        model.addAttribute("endTime", DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS).getTime());
        model.addAttribute("record", record);
        model.addAttribute("member", member);
        return "wechat/banner/lipstick-back";
    }

    /**
     * 活动集赞
     *
     * @return
     */
    @RequestMapping(value = "/lipstick-back2", method = RequestMethod.GET)
    public String activition3(Model model) {
        Member member = ShiroSecurityUtil.getCurrentMember();
        Record record = recordService.isExist(member.getId());
        if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {//参与了活动
            model.addAttribute("limitNumber", record.getNumber());
        } else {
            model.addAttribute("limitNumber", -1);
        }
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                ) {
            model.addAttribute("status", true);//在活动期间
        } else {
            model.addAttribute("status", false);
        }
        model.addAttribute("endTime", DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS).getTime());
        model.addAttribute("record", record);
        model.addAttribute("member", member);
        try {
            model.addAttribute("signature", WeixinUtil.getWxService().createJsapiSignature(getDomainURI()));
        } catch (Exception e) {
            logger.error("积攒授权页面出错：{}", e);
        }
        return "/wechat/banner/lipstick-back2";
    }

    /**
     * 活动口红预热
     *
     * @return
     */
    @RequestMapping(value = "/preheating", method = RequestMethod.GET)
    public String preheating(Model model) {
        return "wechat/banner/preheating";
    }

}
