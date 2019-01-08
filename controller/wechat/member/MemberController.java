package com.cb.controller.wechat.member;

import com.cb.common.core.controller.CommonController;
import com.cb.common.util.*;
import com.cb.model.member.Address;
import com.cb.model.member.Member;
import com.cb.service.common.AddressDatabaseService;
import com.cb.service.member.MemberService;
import com.cb.vo.ResultVo;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 微信端会员请求控制器
 */
@Controller("wechat-memberController")
@RequestMapping("/wechat/member")
public class MemberController extends CommonController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AddressDatabaseService addressDatabaseService;

    /**
     * 注册第一步：填写引荐人（引荐人手机页面）
     *
     * @return
     */
    @RequestMapping(value = "/referrals", method = RequestMethod.GET)
    public String referrals(String code) {
        ResultVo result = WeixinUtil.oauth(request, code, getDomainURI(), memberService, true);
        String redirect = (String) result.get(Constants.REDIRECT_URL);
        if (StringUtil.isNotBlank(redirect)) {
            return redirect;
        }
        return "/wechat/member/referrals";//引荐人手机 在表中配置权限,让接口可以免登录直接访问
    }

    /**
     * 注册第二步：填写手机和密码（填写请求会员注册页面）
     *
     * @param code  微信登录code
     * @param model 数据保存模型
     * @return 返回会员注册页面
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(String code, Model model, String id, String isInvite) {
        id = StringUtil.isBlank(id) ? (String) session.getAttribute("parent") : id;
        if (StringUtil.isNotBlank(isInvite)) {
            session.setAttribute("isInvite", isInvite);
        }
        if (StringUtil.isNotBlank(id)) {//存在id
            Member parent = memberService.findById(id);
            session.setAttribute("parent", id);
            model.addAttribute("parentId", id);
            model.addAttribute("parentName", parent.getRealName());
            model.addAttribute("parentMobile", parent.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        } else {
            return "redirect:/wechat/member/referrals";
        }
        ResultVo result = WeixinUtil.oauth(request, code, getDomainURI(), memberService, true);
        String redirect = (String) result.get(Constants.REDIRECT_URL);
        if (StringUtil.isNotBlank(redirect)) {
            return redirect;
        }
        model.addAttribute("wxMpUser", result.get(WxConsts.OAUTH2_SCOPE_USER_INFO));
        model.addAttribute("levels", Member.LevelEnum.values());
        return "/wechat/member/register";
    }

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
        session.removeAttribute("parent");
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
                                memberService.register(member, parentId, (String) session.getAttribute("isInvite"));
                                session.removeAttribute("isInvite");//移除邀请标识
                                result.success();
                                result.put("url", "/wechat/member/submit?id=" + member.getId());
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
        return JsonUtil.toFullJson(result);
    }

    /**
     * 注册第三步：填写个人信息（请求认证资料填写）
     *
     * @return 返回注册结果
     */
    @RequestMapping(value = "/submit", method = RequestMethod.GET)
    public String submitInformation(String id, Model model) {//修改资料需要id
        Member member = memberService.findById(id);
        Date date = DateUtil.newInstanceDate();
        if (member != null) {
            if (member.getJoinDate() != null) {
                date = member.getJoinDate();
            }
            model.addAttribute("member", member);
            model.addAttribute("status", member.getStatus());//看前端需要什么标识
            String province = member.getProvince();
            String city = member.getCity();
            String area = member.getArea();
            if (StringUtil.isNotBlank(province)) {
                model.addAttribute("province", addressDatabaseService.findById(province));
            }
            if (StringUtil.isNotBlank(city)) {
                model.addAttribute("city", addressDatabaseService.findById(city));
            }
            if (StringUtil.isNotBlank(area)) {
                model.addAttribute("area", addressDatabaseService.findById(area));
            }
        }
        model.addAttribute("date", DateUtil.format(date, "yyyy-MM-dd"));
        model.addAttribute("levels", Member.LevelEnum.values());
        return "/wechat/member/submit";//跳转到修改认证资料
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
                result.put("url", "/wechat/member/results?id=" + member.getId());//跳转到等待审核结果页面
            } else {//之前是未提交审核
                result.put("status", true);//跳转到同意合同页面
            }
            oldMember = memberService.submitInformation(oldMember, member);//修改资料。并且提交审核，更改状态为WIATING
            result.success();
            //推送微信消息：提交了会员申请  完成
            WeixinUtil.sendWxMemberSubmit(oldMember);
        }
        return JsonUtil.toFullJson(result);
    }

    /**
     * 请求查询页面
     *
     * @return 返回查询页面
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search() {
        return "/wechat/member/search";
    }

    /**
     * 请求查询会员
     *
     * @param searchText 查询条件
     * @return 返回查询结果
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public String search(String searchText) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        List<Member> list = memberService.search(searchText);
        if (list != null && list.size() > 0) {
            result.put("success", true);
            result.put("id", list.get(0).getId());
        } else {
            result.put("msg", "未查询到该会员");
        }
        return JsonUtil.toFullJson(result);
    }

    /**
     * 请求会员首页
     *
     * @param model 数据保存模型
     * @param flag  标记是否绑定微信请求
     * @param code  微信授权code
     * @return 返回会员首页
     */
    @RequestMapping("/index")
    public String index(Model model, boolean flag, String code) {
        if (flag || StringUtil.isNotBlank(code)) {
            ResultVo result = WeixinUtil.oauth(request, code, getDomainURI(), memberService, true);
            String redirect = (String) result.get(Constants.REDIRECT_URL);
            if (StringUtil.isNotBlank(redirect)) {
                return redirect;
            }
            model.addAttribute("flag", true);
        }
        Member member = memberService.findById(ShiroSecurityUtil.getCurrentMemberId());
        ShiroSecurityUtil.setAttribute(Constants.SESSION_CURRENT_MEMBER, member);
        model.addAttribute("member", member);
        return "/wechat/member/index";
    }

    /**
     * 请求会员详情页面
     *
     * @param id    会员id
     * @param model 数据保存模型
     * @return 返回会员详情页面
     */
    @RequestMapping("/detail/{id}")
    public String detail(@PathVariable String id, Model model) {
        Member member = memberService.findById(id);
        model.addAttribute("member", member);
        String province = member.getProvince();
        String city = member.getCity();
        String area = member.getArea();
        if (StringUtil.isNotBlank(province)) {
            model.addAttribute("province", addressDatabaseService.findById(province));
        }
        if (StringUtil.isNotBlank(city)) {
            model.addAttribute("city", addressDatabaseService.findById(city));
        }
        if (StringUtil.isNotBlank(area)) {
            model.addAttribute("area", addressDatabaseService.findById(area));
        }
        return "/wechat/member/detail";
    }

    /**
     * 请求会员证书页面
     *
     * @param id    会员id
     * @param model 数据保存模型
     * @return 返回会员证书页面
     */
    @RequestMapping("/certificate")
    public String certificate(String id, Model model) {
        if (StringUtil.isNotBlank(id)) {
            Member member = memberService.findById(id);
            model.addAttribute("member", member);
        }
        return "/wechat/member/certificate";
    }

    /**
     * 请求会员地址管理页面
     *
     * @param model 数据保存模型
     * @return 返回会员地址管理页面
     */
    @RequestMapping(value = "/address", method = RequestMethod.GET)
    public String address(Model model) {
        Member member = memberService.findById(ShiroSecurityUtil.getCurrentMemberId());
        model.addAttribute("member", member);
        return "/wechat/member/address";
    }

    /**
     * 请求保存会员地址
     *
     * @param id          会员id
     * @param addressFull 地址详情
     * @return 返回保存结果
     */
    @RequestMapping(value = "/address", method = RequestMethod.POST)
    @ResponseBody
    public String address(String id, String addressFull) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        if (StringUtil.isNotBlank(id) && StringUtil.isNotBlank(addressFull)) {
            Member member = memberService.findById(id);
            if (member != null) {
                List<Address> list = member.getAddresses();
                if (list != null && list.size() > 0) {
                    Address address = list.get(0);
                    address.setFull(addressFull);
                    memberService.save(member);
                } else {
                    Address address = new Address();
                    address.setFull(addressFull);
                    address.setMember(member);
                    list = new ArrayList<>();
                    list.add(address);
                    member.setAddresses(list);
                    memberService.save(member);
                }
                result.put("success", true);
            } else {
                result.put("msg", "该会员信息有误");
            }
        } else {
            result.put("msg", "参数不能为空");
        }
        return JsonUtil.toFullJson(result);
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
                    resultVo.setMessage("帐号不存在");
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
     * 认证结果页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/results", method = RequestMethod.GET)
    public String results(Model model, String id) {
        Member member = memberService.findById(id);
        model.addAttribute("member", member);
        if (member != null) {
            if (Member.StatusEnum.FAILED.equals(member.getStatus())) {
                model.addAttribute("status", "FAILED");
            } else if (Member.StatusEnum.WAITING.equals(member.getStatus())) {
                model.addAttribute("status", "WAITING");
            } else if (Member.StatusEnum.VERIFY.equals(member.getStatus())) {
                model.addAttribute("status", "VERIFY");
            } else if (Member.StatusEnum.SUCCESS.equals(member.getStatus())) {
                model.addAttribute("status", "SUCCESS");
            } else if (Member.StatusEnum.UNSUBMIT.equals(member.getStatus())) {
                model.addAttribute("status", "UNSUBMIT");
            }
        }
        return "/wechat/member/results";
    }

    /**
     * 认证结果页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/gotoreferrals", method = RequestMethod.GET)
    public String gotoreferrals(Model model) {
        return "/wechat/member/gotoreferrals";
    }

    /**
     * 前往系统设置页面
     */
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String setting(Model model) {
        model.addAttribute("member", ShiroSecurityUtil.getCurrentMember());
        return "/wechat/member/setting";
    }

    /**
     * 前往上级认证审核页面
     */
    @RequestMapping(value = "/verify", method = RequestMethod.GET)
    public String verify(String id, Model model) {
        if (StringUtil.isNotBlank(id)) {
            Member member = memberService.findById(id);
            if (member != null) {
                model.addAttribute("member", member);
            }
        }
        return "/wechat/member/invite/verify";
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
                    return JsonUtil.toFullJson(result);
                }
            }
            result.setMessage("参数有误");
        } else {
            result.setMessage("参数不能为空");
        }
        return JsonUtil.toFullJson(result);
    }

    @RequestMapping(value = "/verify-list", method = RequestMethod.GET)
    public String verifyList(Model model){
        model.addAttribute("members", memberService.findVerifyList());
        return "wechat/member/invite/verifylist";
    }
}
