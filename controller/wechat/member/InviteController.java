package com.cb.controller.wechat.member;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.WeixinUtil;
import com.cb.model.member.Member;
import com.cb.service.member.InviteService;
import com.cb.service.member.MemberService;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.exception.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/2/14.
 */
@Controller("wechat-inviteController")
@RequestMapping("/wechat/member/invite")
public class InviteController extends CommonController {

    @Autowired
    private InviteService inviteService;

    @Autowired
    private MemberService memberService;

    /**
     * 请求获取当前登录会员邀请记录页面
     *
     * @param model 数据存放模型
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Integer pageNo, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<Member> page = new Page<Member>(pageNo, 10);
        Map<String, Object> params = new HashMap<>();
        params.put("inviter_id", ShiroSecurityUtil.getCurrentMemberId());
        ArrayList<Member> members = (ArrayList<Member>) inviteService.findListById(ShiroSecurityUtil.getCurrentMemberId(), params);
//        page = inviteService.findPageByParams(page, params);
        model.addAttribute("page", members);
        return "wechat/member/invite/list";
    }

    /**
     * 请求获取当前登录会员邀请记录页面
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public Page<Member> list(Integer pageNo) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<Member> page = new Page<Member>(pageNo, 10);
        Map<String, Object> params = new HashMap<>();
        params.put("inviter_id", ShiroSecurityUtil.getCurrentMemberId());
        page = inviteService.findPageByParams(page, params);
        return page;
    }

    /**
     * 进入邀请页
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/invition", method = RequestMethod.GET)
    public String invition(Model model) {
        Member member = memberService.findById(ShiroSecurityUtil.getCurrentMemberId());
        try {
            WxJsapiSignature signature = WeixinUtil.getWxService().createJsapiSignature(getQueryStringDomainURI());
            model.addAttribute("signature", signature);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        model.addAttribute("member", member);
        return "wechat/member/invite/invition";
    }

    /**
     * 进入邀请好友
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/friend", method = RequestMethod.GET)
    public String friend(Model model) {
        return "wechat/member/invite/list";
    }

    /**
     * 进入上级验证页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/verify", method = RequestMethod.GET)
    public String verify(Model model) {
        return "wechat/member/invite/verify";
    }


}
