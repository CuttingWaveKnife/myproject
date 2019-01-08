package com.cb.controller.wechat.member;

import com.cb.common.core.controller.CommonController;
import com.cb.common.util.ShiroSecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by GuoMIn on 2017/2/14.
 */
@Controller("wechat-scoreController")
@RequestMapping("/wechat/member/score")
public class ScoreController extends CommonController {

    /**
     * 请求查看会员女神券信息
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String score(Model model) {
        model.addAttribute("member", ShiroSecurityUtil.getCurrentMember());
        return "wechat/member/score/list";
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public String findScore(Integer size, Integer no) {
        return null;
    }

    @RequestMapping(value = "/help", method = RequestMethod.GET)
    public String help() {
        return "wechat/member/score/help";
    }
}
