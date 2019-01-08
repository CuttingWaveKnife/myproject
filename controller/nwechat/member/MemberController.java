package com.cb.controller.nwechat.member;

import com.cb.common.core.controller.CommonController;
import com.cb.common.util.Constants;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.common.util.WeixinUtil;
import com.cb.model.member.Member;
import com.cb.service.common.AddressDatabaseService;
import com.cb.service.member.MemberService;
import com.cb.vo.ResultVo;
import com.cb.vo.member.MemberVo;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 微信端会员请求控制器
 */
@Controller("nwechat-memberController")
@RequestMapping("/nwechat/member")
public class MemberController extends CommonController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AddressDatabaseService addressDatabaseService;

    /**
     * 请求注册会员
     *
     * @param member 会员
     * @param code   手机验证码
     * @return 返回注册结果
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public String register(Member member, String code, String parentId) {
        ResultVo result = new ResultVo();
        String mobile = member.getMobile();
        if (StringUtil.isNotBlank(mobile)) {
            String lastTime = getTime();
            if (StringUtil.isBlank(lastTime) || System.currentTimeMillis() - Long.valueOf(lastTime) < 15 * 60 * 1000) {
                if (StringUtil.equals(mobile, getMobile()) && StringUtil.equals(code, getCode())) {
                    if (memberService.isExist(mobile)) {
                        result.setMessage("手机号码已经被使用");
                    } else {
                        if (StringUtil.isBlank(parentId)) {
                            result.setMessage("引荐人不存在");
                        } else {
                            if (StringUtil.isBlank(member.getUser().getPassword())) {
                                result.setMessage("密码不能为空");
                            } else {
                                WxMpUser wxMpUser = (WxMpUser) ShiroSecurityUtil.getAttribute(WxConsts.OAUTH2_SCOPE_USER_INFO);
                                if (wxMpUser != null) {
                                    member.setOpenId(wxMpUser.getOpenId());
                                    member.setHead(wxMpUser.getHeadImgUrl());
                                    member.getUser().setNickname(wxMpUser.getNickname());
                                    member.setSex(wxMpUser.getSexId().shortValue());
                                } else {
                                    logger.warn("用户注册手机号码时未获得微信授权的用户信息，手机号码：{}", member.getMobile());
                                }
                                memberService.register(member, parentId, (String) session.getAttribute("isInvite"));
                                result.success();
                            }
                        }
                    }
                } else {
                    result.setMessage("短信验证码错误");
                }
            } else {
                result.setMessage("短信验证码已过时");
            }
        }
        return result.toJsonString();
    }

    /**
     * 请求认证资料填写
     *
     * @param member 会员
     * @return 返回注册结果
     */
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    @ResponseBody
    public String submitInformation(Member member) {//修改资料需要id 暂时写这里
        ResultVo result = new ResultVo();
        Member oldMember = memberService.findById(member.getId());
        if (StringUtil.isNotBlank(oldMember)) {
            if (Member.StatusEnum.FAILED.equals(oldMember.getStatus())) {//之前是审核驳回
                result.put("status", false);//跳转到等待审核结果页面
            } else {//之前是未提交审核
                result.put("status", true);//跳转到同意合同页面
            }
            oldMember = memberService.submitInformation(oldMember, member);//修改资料。并且提交审核，更改状态为WIATING
            result.success();
            //推送微信消息：提交了会员申请  完成
            WeixinUtil.sendWxMemberSubmit(oldMember);
        } else {
            result.setMessage("用户不存在");
        }
        return result.toJsonString();
    }

    /**
     * 请求查询会员
     *
     * @param search 查询条件
     * @return 返回查询结果
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public String search(String search) {
        ResultVo result = new ResultVo();
        List<Member> list = memberService.search(search);
        if (list != null && list.size() > 0) {
            result.success();
            result.put("member", MemberVo.toMemberVo(list.get(0)));
        } else {
            result.setMessage("未查询到该会员");
        }
        return result.toJsonString();
    }

    /**
     * 请求会员信息
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public String get() {
        ResultVo result = new ResultVo();
        String memberId = ShiroSecurityUtil.getCurrentMemberId();
        if (StringUtil.isNotBlank(memberId)) {
            Member member = memberService.findById(ShiroSecurityUtil.getCurrentMemberId());
            if (member != null) {
                ShiroSecurityUtil.setAttribute(Constants.SESSION_CURRENT_MEMBER, member);
                MemberVo vo = MemberVo.toMemberVo(member);
                String province = member.getProvince();
                String city = member.getCity();
                String area = member.getArea();
                if (StringUtil.isNotBlank(province)) {
                    vo.setProvince(addressDatabaseService.findById(province).getName());
                }
                if (StringUtil.isNotBlank(city)) {
                    vo.setCity(addressDatabaseService.findById(city).getName());
                }
                if (StringUtil.isNotBlank(area)) {
                    vo.setArea(addressDatabaseService.findById(area).getName());
                }
                result.put("member", vo);
                result.success();
            } else {
                result.setMessage("会员不存在");
            }
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }

    /**
     * 绑定微信
     *
     * @return 返回绑定结果
     */
    @RequestMapping(value = "/bind", method = RequestMethod.POST)
    @ResponseBody
    public String bind(String code) {
        ResultVo resultVo = new ResultVo();
        String memberId = ShiroSecurityUtil.getCurrentMemberId();
        if (memberId != null) {
            ResultVo result = WeixinUtil.oauth(request, code, getDomainURI(), memberService, true);
            String redirect = (String) result.get(Constants.REDIRECT_URL);
            if (StringUtil.isNotBlank(redirect)) {
                return redirect;
            }
            if (result.get(Constants.SESSION_CURRENT_MEMBER) == null) {
                WxMpUser wxMpUser = (WxMpUser) result.get(WxConsts.OAUTH2_SCOPE_USER_INFO);
                Member member = memberService.findById(memberId);
                if (member != null) {
                    member.setOpenId(wxMpUser.getOpenId());
                    member.setHead(wxMpUser.getHeadImgUrl());
                    member.getUser().setNickname(wxMpUser.getNickname());
                    member.setSex(wxMpUser.getSexId().shortValue());
                    memberService.save(member);
                    ShiroSecurityUtil.setAttribute(Constants.SESSION_CURRENT_MEMBER, member);
                    resultVo.success();
                } else {
                    result.setMessage("会员不存在");
                }
            } else {
                resultVo.setMessage("绑定失败，此微信已有绑定帐号");
            }
        } else {
            resultVo.setMessage("未登录");
        }
        return resultVo.toJsonString();
    }

    /**
     * 请求解除微信绑定
     *
     * @return 返回解除结果
     */
    @RequestMapping(value = "/unbound", method = RequestMethod.POST)
    @ResponseBody
    public String unbound() {
        ResultVo result = new ResultVo();
        String memberId = ShiroSecurityUtil.getCurrentMemberId();
        if (StringUtil.isNotBlank(memberId)) {
            Member member = memberService.findById(memberId);
            if (member != null) {
                try {
                    member.setOpenId(null);
                    memberService.save(member);
                    ShiroSecurityUtil.setAttribute(Constants.SESSION_CURRENT_MEMBER, member);
                    result.success();
                    logger.warn("会员{}({})进行了解除微信绑定操作！", member.getRealName(), member.getMobile());
                } catch (Exception e) {
                    logger.error("会员解除微信绑定出错：{}", e);
                }
            } else {
                result.setMessage("会员不存在");
            }
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }

    /**
     * 上级认证审核
     *
     * @param value 预留审核原因
     * @return
     */
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    @ResponseBody
    public String audit(String id, String type, String value) {
        ResultVo result = new ResultVo();
        Member member = memberService.findById(id);
        if (StringUtil.isNotBlank(id) && StringUtil.isNotBlank(type)) {
            for (Member.StatusEnum status : Member.StatusEnum.values()) {
                if (type.equals(status.name())) {
                    member.setStatus(status);
                    memberService.save(member);
                    result.success();
                    result.setMessage(status.getDesc());
                    if ("FAILED".equals(type)) {
                        //推送微信消息： 上级审核驳回
                        WeixinUtil.sendWxMemberFailed(member, "parent");
                    }
                }
            }
            result.setMessage("参数有误");
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }
}
