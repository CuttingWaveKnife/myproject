package com.cb.controller.system.member;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.*;
import com.cb.model.member.Member;
import com.cb.model.member.ScoreRecord;
import com.cb.service.common.AddressDatabaseService;
import com.cb.service.member.MemberService;
import com.cb.service.member.ScoreRecordService;
import com.cb.vo.ResultVo;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理系统会员请求控制器
 */
@Controller("sys-memberController")
@RequestMapping("/system/member")
public class MemberController extends CommonController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AddressDatabaseService addressDatabaseService;

    @Autowired
    private ScoreRecordService scoreRecordService;

    /**
     * 请求会员列表页面
     *
     * @param model 数据存放模型
     * @return 返回会员列表页面
     */
    @RequestMapping("/list")
    public String index(Model model) {
        model.addAttribute("levels", Member.LevelEnum.values());
        model.addAttribute("status", Member.StatusEnum.values());
        return "/system/member/list";
    }

    /**
     * 请求按条件分页查找会员
     *
     * @param search     查询条件
     * @param datepicker 授权时间
     * @param level      等级
     * @param province   省
     * @param city       市
     * @param area       区
     * @param status     状态
     * @param pageNo     页码
     * @param model      数据存放模型
     * @return 返回查询结果集
     */
    @RequestMapping("/find")
    public String find(String search, String datepicker, String level, String province, String city, String area, String status, Integer pageNo, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<Member> page = new Page<>(pageNo, 10);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("search", search);
        }
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("start", temps[0]);
                params.put("end", temps[1]);
            }
        }
        if (StringUtil.isNotBlank(level)) {
            params.put("level", level);
        }
        if (StringUtil.isNotBlank(province)) {
            params.put("province", province);
        }
        if (StringUtil.isNotBlank(city)) {
            params.put("city", city);
        }
        if (StringUtil.isNotBlank(area)) {
            params.put("area", area);
        }
        if (StringUtil.isNotBlank(status)) {
            params.put("status", status);
        }
        params.put("nostatus", new String[]{Member.StatusEnum.VERIFY.name(), Member.StatusEnum.UNSUBMIT.name()});//未提交，等待上级审核不显示
        params.put("audit_user_id", ShiroSecurityUtil.getCurrentUserId());
        boolean[] booleans = ShiroSecurityUtil.getSubject().hasRoles(ListUtil.arrayToList(new String[]{"super", "management"}));
        for (boolean aBoolean : booleans) {
            if (aBoolean) {
                params.remove("audit_user_id");
                params.remove("nostatus");
            }
        }
        page = memberService.findPageByParams(page, params);
        model.addAttribute("page", page);
        return "/system/member/find";
    }

    /**
     * 请求会员编辑页面
     *
     * @param id    会员id
     * @param model 数据存放模型
     * @return 返回会员编辑页面
     */
    @RequestMapping("/edit")
    public String edit(String id, Model model) {
        List<Member> allParents = memberService.findListByStatus(Member.StatusEnum.SUCCESS);
        if (StringUtil.isNotBlank(id)) {
            Member member = memberService.findById(id);
            model.addAttribute("member", member);
            allParents.remove(member);
        }
        model.addAttribute("allParents", allParents);
        model.addAttribute("levels", Member.LevelEnum.values());
        return "/system/member/edit";
    }

    /**
     * 请求保存会员
     *
     * @param member 会员
     * @return 返回保存结果
     */
    @RequestMapping("/save")
    @ResponseBody
    public String save(Member member) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        try {
            memberService.edit(member);
            result.put("success", true);
        } catch (Exception e) {
            logger.error("修改会员信息出错：{}", e);
        }
        return JsonUtil.toFullJson(result);
    }

    /**
     * 请求会员详情页面
     *
     * @param id    会员id
     * @param model 数据存放模型
     * @return 返回会员详情页面
     */
    @RequestMapping("/detail/{id}")
    public String detail(@PathVariable String id, Model model) {
        Member member = memberService.findById(id);
        if (member != null) {
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
            model.addAttribute("levels", Member.LevelEnum.values());
        }
        return "/system/member/detail";
    }

    /**
     * 请求删除会员
     *
     * @param ids 所有需要删除的会员id
     * @return 返回删除结果
     */
    @RequestMapping("/delete")
    @ResponseBody
    public String delete(String ids) {
        ResultVo result = new ResultVo();
        try {
            ShiroSecurityUtil.getSubject().checkRole("super");  //判断是否有是超级管理员
            if (StringUtil.isNotBlank(ids)) {
                int num = memberService.batchDelete(ids.split(","));
                result.success();
                result.setMessage("成功删除，共影响{0}条数据", num + "");
                logger.warn("用户{}删除会员{}，此操作共影响数据库{}条数据！", ShiroSecurityUtil.getCurrentUserId(), ids, num);
            } else {
                result.setMessage("参数不能为空");
            }
        } catch (UnauthorizedException e) {
            result.setMessage("没有删除权限");
        } catch (Exception e) {
            logger.error("删除会员信息出错{}", e);
        }
        return JsonUtil.toFullJson(result);
    }

    /**
     * 请求审核会员
     *
     * @param id   会员id
     * @param type 审核类型（通过审核，不通过审核）
     * @return 返回审核结果
     */
    @RequestMapping("/audit")
    @ResponseBody
    public String audit(String id, String type, String value) {
        ResultVo result = new ResultVo();
        Member member = memberService.findById(id);
        if (StringUtil.isNotBlank(id) && StringUtil.isNotBlank(type)) {
            for (Member.StatusEnum status : Member.StatusEnum.values()) {
                if (type.equals(status.name())) {
                    memberService.changeStatus(id, status, value);
                    result.success();
                    result.setMessage(status.getDesc());
                    if ("SUCCESS".equals(type)) {
                        //推送微信消息：审核通过已经成为会员 完成
                        WeixinUtil.sendWxMemberSuccess(member);
                    } else {
                        //推送微信消息：审核不通过 完成
                        WeixinUtil.sendWxMemberFailed(member, "");
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

    /**
     * 请求积分调整
     *
     * @param id     会员id
     * @param score  调整积分数量
     * @param status 类型
     * @param remark 备注信息
     * @return 返回调整结果
     */
    @RequestMapping(value = "/score/adjust")
    @ResponseBody
    public String adjustScore(String id, Integer score, ScoreRecord.StatusEnum status, String remark) {
        ResultVo result = new ResultVo();
        try {
            ShiroSecurityUtil.getSubject().checkRole("super");  //判断是否有是超级管理员
            if (StringUtil.isNotBlank(id) && score != null) {
                Member member = memberService.findById(id);
                if (member != null) {
                    Integer allScore = member.getScore();
                    if (status.equals(ScoreRecord.StatusEnum.ACTIVATION)) {
                        Integer avScore = member.getAvailableScore();
                        if (score < 0 && (allScore < -score || avScore < -score)) {
                            result.setMessage("会员的女神券不足");
                        } else {
                            member.setScore(allScore + score);
                            member.setAvailableScore(avScore + score);
                            ScoreRecord scoreRecord = new ScoreRecord("系统调整", score, status, remark, member);
                            scoreRecordService.save(scoreRecord);
                            result.success();
                        }
                    } else {
                        Integer unScore = member.getUnavailableScore();
                        if (score < 0 && (allScore < -score || unScore < -score)) {
                            result.setMessage("会员的女神券不足");
                        } else {
                            member.setScore(allScore + score);
                            member.setUnavailableScore(unScore + score);
                            ScoreRecord scoreRecord = new ScoreRecord("系统调整", score, status, remark, member);
                            scoreRecordService.save(scoreRecord);
                            result.success();
                        }
                    }
                } else {
                    result.setMessage("会员不存在");
                }
            } else {
                result.setMessage("参数不能为空");
            }
        } catch (UnauthorizedException e) {
            result.setMessage("没有调整权限");
        } catch (Exception e) {
            logger.error("调整女神券出错{}", e);
        }
        return result.toJsonString();
    }
}
