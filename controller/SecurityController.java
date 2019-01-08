package com.cb.controller;

import com.cb.common.core.controller.CommonController;
import com.cb.common.exception.*;
import com.cb.common.shiro.MyToken;
import com.cb.common.util.*;
import com.cb.model.member.Member;
import com.cb.model.security.User;
import com.cb.service.member.MemberService;
import com.cb.service.security.UserService;
import com.cb.vo.ResultVo;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 登录、登出控制器
 */
@Controller("securityController")
@RequestMapping("/security")
public class SecurityController extends CommonController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    /**
     * 系统退出登录
     */
    @RequestMapping(value = "/logout-system", method = RequestMethod.GET)
    public String logout() {
        ShiroSecurityUtil.getSubject().logout();
        return "redirect:/security/login-system";
    }

    /**
     * 微信退出登录
     */
    @RequestMapping(value = "/logout-wechat", method = RequestMethod.GET)
    public String logout2() {
        ShiroSecurityUtil.getSubject().logout();
        return "redirect:/security/login-wechat?flag=true";
    }

    /**
     * 前往登录页面
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return getReturnPath();
    }

    /**
     * 前往系统登录页面
     */
    @RequestMapping(value = "/login-system", method = RequestMethod.GET)
    public String loginSystem() {
        return "/security/login-system";
    }

    /**
     * 前往微信公众号会员系统登录页面
     */
    @RequestMapping(value = "/login-wechat", method = RequestMethod.GET)
    public String loginWechat(boolean flag, String code) {
        //判断是否是微信浏览器
        if (isMicromessenger()) {
            try {
                //用户微信授权
                ResultVo r = WeixinUtil.oauth(request, code, getDomainURI(), memberService, flag);
                String redirect = (String) r.get(Constants.REDIRECT_URL);
                if (StringUtil.isNotBlank(redirect)) {
                    return redirect;
                }
                if (!flag) {
                    Member member = (Member) r.get(Constants.SESSION_CURRENT_MEMBER);//取出微信授权保存的用户信息
                    User user = member.getUser();
                    String result = doLogin(user.getUsername(), user.getPassword(), user.getType().name(), true);
                    ResultVo resultVo = JsonUtil.fromJson(result, ResultVo.class);
                    if (resultVo != null && resultVo.isSuccess()) {
                        return "redirect:" + StringUtil.removeStart((String) resultVo.get("url"), request.getContextPath());
                    }
                }
            } catch (Exception e) {
                logger.warn("微信自动登录报错：{}", e.getMessage());
            }
        }
        return "/security/login-wechat";
    }

    /**
     * 进行登录操作
     *
     * @param username 登录帐号
     * @param password 密码
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String doLogin(String username, String password, String type, boolean encrypted) {
        ResultVo result = new ResultVo();
        String message = "系统错误";
        if (StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
            message = "参数不能为空";
        } else {
            MyToken token = new MyToken(username, password, User.TypeEnum.valueOf(type), encrypted);
//            token.setRememberMe(true);
            Subject subject = ShiroSecurityUtil.getSubject();
            User user = userService.getUserByUsername(username);
            try {
                subject.login(token);
                if (subject.isAuthenticated()) {
                    message = "登录成功";
                    result.success();

                    if (User.TypeEnum.SYSTEM.name().equals(type)) {
                        result.put("url", request.getContextPath() + "/system/index");
                    } else {
                        result.put("url", request.getContextPath() + "/wechat/member/index");
                        if (isMicromessenger()) {
                            try {
                                WxMpOAuth2AccessToken wxMpOAuth2AccessToken = (WxMpOAuth2AccessToken) session.getAttribute(Constants.OAUTH_ACCESS_TOKEN);
                                logger.info("微信手动登录时从session中拿到的微信授权信息：{}", wxMpOAuth2AccessToken);
                                WxMpUser wxMpUser = WeixinUtil.getWxService().oauth2getUserInfo(wxMpOAuth2AccessToken, null);   //根据授权信息请求获取wx用户信息
                                Member member = user.getMember();
                                if (StringUtil.isBlank(member.getOpenId())) {
                                    member.setOpenId(wxMpUser.getOpenId());
                                    member.setHead(wxMpUser.getHeadImgUrl());
                                    member.getUser().setNickname(wxMpUser.getNickname());
                                    member.setSex(wxMpUser.getSexId().shortValue());
                                    //如果会员的头像、昵称、性别有改变，则更新会员信息
                                    memberService.save(member);
                                    logger.info("微信手动登录时更改会员信息");
                                }
                            } catch (Exception e) {
                                logger.error("微信手动登录时更改会员信息出错：{}", e);
                            }
                        }
                    }
                    SavedRequest savedRequest = WebUtils.getSavedRequest(request);
                    if (savedRequest != null) {
                        String url = savedRequest.getRequestUrl();
                        result.put("url", url);
                    }
                } else {
                    message = "登录失败";
                }
            } catch (WaitingException e) {
                message = "帐号正在审核";
                result.put("status", true);
                result.put("url", "/wechat/member/results?id=" + user.getMember().getId());//等待审核页面
            } catch (UnsubmitException e) {
                message = "帐号未提交审核";
                result.put("status", true);
                result.put("url", "/wechat/member/submit?id=" + user.getMember().getId());//填写资料页面?需要带id
            } catch (VerifyException e) {
                message = "等待上级审核";
                result.put("status", true);
                result.put("url", "/wechat/member/results?id=" + user.getMember().getId());//等待审核页面 需要带id
            } catch (FailedException e) {
                message = "帐号审核未通过";
                result.put("status", true);
                result.put("url", "/wechat/member/results?id=" + user.getMember().getId());//审核结果页面
            } catch (UnknownAccountException | IncorrectCredentialsException e) {
                result.put("status", false);
                message = "密码错误或用户名不存在";
            } catch (ExcessiveAttemptsException e) {
                result.put("status", false);
                message = "登录失败次数过多";
            } catch (LockedAccountException e) {
                result.put("status", false);
                message = "帐号已被锁定";
            } catch (DisabledAccountException e) {
                result.put("status", false);
                message = "帐号已被禁用";
            } catch (ExpiredCredentialsException e) {
                message = "帐号已过期";
                result.success();
                result.put("url", request.getContextPath() + "/security/login-wechat?flag=true");
            } catch (UnauthorizedException e) {
                message = "您没有得到相应的授权";
            } catch (Exception e) {
                result.put("status", false);
                e.printStackTrace();
            }
            result.setMessage(message);
        }
        return result.toJsonString();
    }

    /**
     * 请求重置密码
     *
     * @param password 密码
     * @return 返回重置结果
     */
    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    @ResponseBody
    public String resetPassword(String username, String code, String password) {
        ResultVo result = new ResultVo();
        User user = null;
        if (StringUtil.isNotBlank(username) && StringUtil.isNotBlank(code)) {
            String lastTime = getTime();
            if (StringUtil.isBlank(lastTime) || System.currentTimeMillis() - Long.valueOf(lastTime) < 15 * 60 * 1000) {
                if (StringUtil.equals(username, getMobile()) && StringUtil.equals(code, getCode())) {
                    user = userService.getUserByUsername(username);
                } else {
                    result.setMessage("短信验证码错误");
                    return result.toJsonString();
                }
            } else {
                result.setMessage("短信验证码已过时");
                return result.toJsonString();
            }
        } else if (StringUtil.isBlank(username) && StringUtil.isBlank(code)) {
            user = ShiroSecurityUtil.getCurrentUser();
        } else {
            result.setMessage("参数不能为空");
            return result.toJsonString();
        }
        if (user != null && user.getType().equals(User.TypeEnum.WECHAT)) {
            if (StringUtil.isNotBlank(password)) {
                try {
                    userService.resetPassword(user, password);
                    result.success();
                } catch (AppServiceException e) {
                    logger.error("用户重置密码失败：{}", e.getMessage());
                    result.setMessage(e.getMessage());
                } catch (Exception e) {
                    logger.error("用户重置密码失败：{}", e);
                }
            } else {
                result.setMessage("参数不能为空");
            }
        } else {
            result.setMessage("用户不存在");
        }
        return result.toJsonString();
    }

    /**
     * 判断当前请求路径是管理系统还是微信公众号会员系统
     */
    private String getReturnPath() {
        SavedRequest savedRequest = WebUtils.getSavedRequest(request);
        String url = "/";
        if (savedRequest != null) {
            url = savedRequest.getRequestUrl();
            if (url.contains("/system")) {
                url = "/security/login-system";
            } else if (url.contains("/wechat")) {
                url = "/security/login-wechat";
            }
        }
        return "redirect:" + url;
    }

    /**
     * 前往忘记密码页面
     */
    @RequestMapping(value = "/change-password", method = RequestMethod.GET)
    public String changePassword(String is, Model model) {
        Member member = ShiroSecurityUtil.getCurrentMember();
        if (member != null && is.equals("true")) {//进入修改密码
            model.addAttribute("is", "true");
        }
        if (is.equals("false")) {//进入忘记密码
            model.addAttribute("is", "false");
        }
        return "/security/change-password";
    }

}