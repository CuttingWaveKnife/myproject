package com.cb.controller.nwechat.member;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.member.ScoreRecord;
import com.cb.service.member.ScoreRecordService;
import com.cb.vo.ResultVo;
import com.cb.vo.member.ScoreRecordVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by GuoMIn on 2017/2/14.
 */
@Controller("nwechat-scoreController")
@RequestMapping("/nwechat/member/score")
public class ScoreController extends CommonController {

    @Autowired
    private ScoreRecordService scoreRecordService;

    /**
     * 请求查看会员女神券信息
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public String score(Integer pageNo) {
        ResultVo result = new ResultVo();
        String memberId = ShiroSecurityUtil.getCurrentMemberId();
        if (StringUtil.isNotBlank(memberId)) {
            pageNo = pageNo == null ? 1 : pageNo;
            Page<ScoreRecord> page = new Page<>(pageNo, 10);
            page = scoreRecordService.findPageByMemberId(page, ShiroSecurityUtil.getCurrentMemberId());
            result.put("page", ScoreRecordVo.toPageVo(page));
            result.success();
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }
}
