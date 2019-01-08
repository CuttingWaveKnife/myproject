package com.cb.controller.system;

import com.cb.common.core.controller.CommonController;
import com.cb.common.util.Constants;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.model.member.Member;
import com.cb.model.security.User;
import com.cb.service.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by l on 2016/11/24.
 */
@Controller("sys-indexController")
@RequestMapping("/system/index")
public class IndexController extends CommonController {

    @Autowired
    private MemberService memberService;

    /**
     * 请求首页
     *
     * @param model 数据保存模型
     * @return 返回首页
     */
    @RequestMapping
    public String index(Model model) {
        model.addAttribute("nickname", ShiroSecurityUtil.getNickname());
        return "/system/index";
    }

    /**
     * 请求统计信息
     *
     * @param model 数据保存模型
     * @return 返回统计信息
     */
    @RequestMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("map", memberService.statistics());
        model.addAttribute("members", memberService.findListByStatus(Member.StatusEnum.WAITING));
        return "/system/statistics";
    }
}
