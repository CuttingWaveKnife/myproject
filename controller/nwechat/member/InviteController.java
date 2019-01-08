package com.cb.controller.nwechat.member;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.member.Member;
import com.cb.service.member.InviteService;
import com.cb.vo.ResultVo;
import com.cb.vo.member.MemberVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/2/14.
 */
@Controller("nwechat-inviteController")
@RequestMapping("/nwechat/member/invite")
public class InviteController extends CommonController {

    @Autowired
    private InviteService inviteService;

    /**
     * 请求获取当前登录会员邀请记录页面
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public String list(Integer pageNo) {
        ResultVo result = new ResultVo();
        String memberId = ShiroSecurityUtil.getCurrentMemberId();
        if (StringUtil.isNotBlank(memberId)) {
            pageNo = pageNo == null ? 1 : pageNo;
            Page<Member> page = new Page<Member>(pageNo, 10);
            Map<String, Object> params = new HashMap<>();
            params.put("inviter_id", memberId);
            page = inviteService.findPageByParams(page, params);
            result.put("memberVoList", MemberVo.toPageVo(page));
            result.success();
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }

}
