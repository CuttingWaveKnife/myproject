package com.cb.controller.wechat.active;

import com.cb.common.util.*;
import com.cb.controller.common.CommonController;
import com.cb.model.active.Record;
import com.cb.model.member.Member;
import com.cb.service.activition.RecordService;
import com.cb.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yangjin 2017/4/24
 */
@Controller("wechat-recordController")
@RequestMapping("/wechat/active/record")
public class RecordController extends CommonController {

    @Autowired
    private RecordService recordService;

    @RequestMapping(value = "/save-img", method = RequestMethod.POST)
    @ResponseBody
    public String saveImg(String img) {
        ResultVo result = new ResultVo();
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_IMG_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                ) {
            if (StringUtil.isNotBlank(img)) {
                Member member = ShiroSecurityUtil.getCurrentMember();
                Record record = recordService.isExist(member.getId());
                if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {
                    record.setImgUrl(img);
                    recordService.save(record);
                    logger.warn("上传图片信息：会员{}({})上传图片{}", member.getRealName(), member.getMobile(), img);
                    result.success();
                } else {
                    result.setMessage("未参与活动");
                }
            } else {
                result.setMessage("参数不能为空");
            }
        } else {
            result.setMessage("不在活动时间");
        }
        return result.toJsonString();
    }
}
