package com.cb.controller.system;

import com.cb.common.core.controller.CommonController;
import com.cb.common.util.DateUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.member.Member;
import com.cb.service.member.MemberService;
import com.cb.service.order.OrderService;
import com.cb.vo.Info;
import com.cb.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.*;

@Controller("system-statisticsController")
@RequestMapping("/system/statistics")
public class StatisticsController extends CommonController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/member", method = RequestMethod.GET)
    public String member(String daterangepicker, Model model) {
        Map<String, Object> params = new HashMap<>();
        daterangepicker = checkTime(daterangepicker, params);
        try {
            model.addAttribute("statistics", memberService.statistics(params));
            model.addAttribute("daterangepicker", daterangepicker);
        } catch (Exception e) {
            logger.error("统计查询会员信息出错{}", e);
        }
        return "/system/statistics/member";
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public String order(String daterangepicker, Member.LevelEnum level, Model model) {
        Map<String, Object> params = new HashMap<>();
        daterangepicker = checkTime(daterangepicker, params);
        params.put("level", level);
        try {
            model.addAttribute("statistics", orderService.statistics(params));
            model.addAttribute("daterangepicker", daterangepicker);
        } catch (Exception e) {
            logger.error("统计查询订单信息出错{}", e);
        }
        model.addAttribute("level", Member.LevelEnum.values());
        return "/system/statistics/order";
    }

    @RequestMapping(value = "/rank", method = RequestMethod.GET)
    public String rank(String daterangepicker, Member.LevelEnum level, String sort, Model model) {
        Map<String, Object> params = new HashMap<>();
        daterangepicker = checkTime(daterangepicker, params);
        params.put("level", level);
        if ("number".equals(sort)) {
            params.put("$sort", "ORDER BY amount DESC");
        } else {
            params.put("$sort", "ORDER BY amount DESC");
        }
        try {
            model.addAttribute("statistics", orderService.rank(params));
            model.addAttribute("daterangepicker", daterangepicker);
        } catch (Exception e) {
            logger.error("统计查询订单信息出错{}", e);
        }
        model.addAttribute("level", Member.LevelEnum.values());
        return "/system/statistics/rank";
    }

    @RequestMapping(value = "/active", method = RequestMethod.GET)
    public String active(String daterangepicker, Model model) {
        Map<String, Object> params = new HashMap<>();
        daterangepicker = checkTime(daterangepicker, params);
        try {
            model.addAttribute("statistics", orderService.active(params));
            model.addAttribute("daterangepicker", daterangepicker);
        } catch (Exception e) {
            logger.error("统计查询订单信息出错{}", e);
        }
        return "/system/statistics/active";
    }

    @RequestMapping(value = "/customer", method = RequestMethod.GET)
    public String customer(String daterangepicker, Model model) {
        Map<String, Object> params = new HashMap<>();
        daterangepicker = checkTime(daterangepicker, params);
        try {
            Map<String, Integer> result = new LinkedHashMap<>();
            Date start = DateUtil.addDays(DateUtil.parse((String) params.get("startEnd"), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS), -1);
            Date end = DateUtil.parse((String) params.get("end"), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS);
            Long size = (end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000);
            Integer add = 0;
            for (int i = 0; i <= size; i++) {
                Date d = DateUtil.addDays(start, i);
                Integer number = orderService.customer(d);
                if (i != 0) {
                    result.put(DateUtil.format(d, DateUtil.DateFormatter.FORMAT_YYYY_MM_DD), number - add);
                }
                add = number;
            }
            model.addAttribute("result", result);
            model.addAttribute("daterangepicker", daterangepicker);
        } catch (Exception e) {
            logger.error("统计查询会员信息出错{}", e);
        }
        return "/system/statistics/customer";
    }

    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    public String tree(Model model, String search, String daterangepicker, String sort) {
        Map<String, Object> params = new HashMap<>();
        daterangepicker = checkTime(daterangepicker, params);
        try {
            if (StringUtil.isNotBlank(search)) {
                params.put("search", search);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            List<Map<String, Object>> list = memberService.tree(params);
            for (Map<String, Object> map : list) {
                if (StringUtil.isNotBlank(map.get("threeId"))) {
                    String oneId = (String) map.get("oneId");
                    Map<String, Object> one = (Map<String, Object>) result.get(oneId);
                    Info oneInfo;
                    if (one == null) {
                        one = new LinkedHashMap<>();
                        Info info = new Info();
                        info.setId((String) map.get("oneId"));
                        info.setName((String) map.get("oneName"));
                        info.setLevel((String) map.get("oneLevel"));
                        info.setMoney((BigDecimal) map.get("oneMoney"));
                        info.setTeam(1);
                        info.setTeamMoney((BigDecimal) map.get("allMoney"));
                        info.setAll(info.getMoney());
                        one.put(oneId, info);
                    }
                    oneInfo = (Info) one.get(oneId);
                    String twoId = (String) map.get("twoId");
                    Map<String, Object> two = (Map<String, Object>) one.get(twoId);
                    Info twoInfo;
                    if (two == null) {
                        two = new LinkedHashMap<>();
                        Info info = new Info();
                        info.setId((String) map.get("twoId"));
                        info.setName((String) map.get("twoName"));
                        info.setLevel((String) map.get("twoLevel"));
                        info.setMoney((BigDecimal) map.get("twoMoney"));
                        info.setTeam(1);
                        info.setAll(info.getMoney());
                        two.put(twoId, info);
                        oneInfo.setTeam(oneInfo.getTeam() + 1);
                        oneInfo.setAll(oneInfo.getAll().add(info.getMoney()));
                    }
                    twoInfo = (Info) two.get(twoId);
                    List<Info> infos = (List<Info>) two.get("list");
                    if (infos == null) {
                        infos = new ArrayList<>();
                    }
                    Info info = new Info();
                    info.setId((String) map.get("threeId"));
                    info.setName((String) map.get("threeName"));
                    info.setLevel((String) map.get("threeLevel"));
                    info.setMoney((BigDecimal) map.get("threeMoney"));
                    info.setTeam(1);
                    info.setAll(info.getMoney());
                    infos.add(info);
                    oneInfo.setTeam(oneInfo.getTeam() + 1);
                    oneInfo.setAll(oneInfo.getAll().add(info.getMoney()));
                    twoInfo.setTeam(twoInfo.getTeam() + 1);
                    twoInfo.setAll(twoInfo.getAll().add(info.getMoney()));
                    one.put(oneId, oneInfo);
                    two.put(twoId, twoInfo);
                    two.put("list", infos);
                    one.put(twoId, two);
                    result.put(oneId, one);
                } else if (StringUtil.isNotBlank(map.get("twoId"))) {
                    String oneId = (String) map.get("oneId");
                    Map<String, Object> one = (Map<String, Object>) result.get(oneId);
                    Info oneInfo;
                    if (one == null) {
                        one = new LinkedHashMap<>();
                        Info info = new Info();
                        info.setId((String) map.get("oneId"));
                        info.setName((String) map.get("oneName"));
                        info.setLevel((String) map.get("oneLevel"));
                        info.setMoney((BigDecimal) map.get("oneMoney"));
                        info.setTeam(1);
                        info.setTeamMoney((BigDecimal) map.get("allMoney"));
                        info.setAll(info.getMoney());
                        one.put(oneId, info);
                    }
                    oneInfo = (Info) one.get(oneId);
                    String twoId = (String) map.get("twoId");
                    Map<String, Object> two = new LinkedHashMap<>();
                    Info info = new Info();
                    info.setId((String) map.get("twoId"));
                    info.setName((String) map.get("twoName"));
                    info.setLevel((String) map.get("twoLevel"));
                    info.setMoney((BigDecimal) map.get("twoMoney"));
                    info.setTeam(1);
                    info.setAll(info.getMoney());
                    two.put(twoId, info);
                    oneInfo.setTeam(oneInfo.getTeam() + 1);
                    oneInfo.setAll(oneInfo.getAll().add(info.getMoney()));
                    one.put(oneId, oneInfo);
                    one.put(twoId, two);
                    result.put(oneId, one);
                } else {
                    String oneId = (String) map.get("oneId");
                    Map<String, Object> one = new LinkedHashMap<>();
                    Info info = new Info();
                    info.setId((String) map.get("oneId"));
                    info.setName((String) map.get("oneName"));
                    info.setLevel((String) map.get("oneLevel"));
                    info.setMoney((BigDecimal) map.get("oneMoney"));
                    info.setTeam(1);
                    info.setTeamMoney((BigDecimal) map.get("allMoney"));
                    info.setAll(info.getMoney());
                    one.put(oneId, info);
                    result.put(oneId, one);
                }
            }
            if (StringUtil.isNotBlank(sort)) {
                List<Map.Entry<String, Object>> mapList = new ArrayList<>(result.entrySet());
                mapList.sort((e1, e2) -> {
                    Map<String, Object> map1 = (Map<String, Object>) e1.getValue();
                    Info info1 = (Info) map1.get(e1.getKey());
                    Map<String, Object> map2 = (Map<String, Object>) e2.getValue();
                    Info info2 = (Info) map2.get(e2.getKey());
                    int r = 0;
                    if (sort.equals("team")) {
                        r = info2.getTeam() - info1.getTeam();
                    } else {
                        r = info2.getAll().compareTo(info1.getAll());
                    }
                    if (map1.size() > 1) {
                        List<Map.Entry<String, Object>> mapListChild = new ArrayList<>(map1.entrySet());
                        mapListChild.sort((c1, c2) -> {
                            if (c1.getValue() instanceof LinkedHashMap && c2.getValue() instanceof LinkedHashMap) {
                                Map<String, Object> mapc1 = (Map<String, Object>) c1.getValue();
                                Info infoc1 = (Info) mapc1.get(c1.getKey());
                                Map<String, Object> mapc2 = (Map<String, Object>) c2.getValue();
                                Info infoc2 = (Info) mapc2.get(c2.getKey());
                                if (sort.equals("team")) {
                                    return infoc2.getTeam() - infoc1.getTeam();
                                } else {
                                    return infoc2.getAll().compareTo(infoc1.getAll());
                                }
                            }
                            return 1;
                        });
                        map1.clear();
                        for (Map.Entry<String, Object> entry : mapListChild) {
                            map1.put(entry.getKey(), entry.getValue());
                        }
                    }
                    return r;
                });
                result.clear();
                for (Map.Entry<String, Object> entry : mapList) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            model.addAttribute("trees", result);
            model.addAttribute("search", search);
            model.addAttribute("sort", sort);
            model.addAttribute("daterangepicker", daterangepicker);
        } catch (Exception e) {
            logger.error("钻石会员树查询出错{}", e);
        }
        return "/system/statistics/tree";
    }

    @RequestMapping(value = "/diamond", method = RequestMethod.GET)
    public String diamond() {
        return "/system/statistics/diamond";
    }

    @RequestMapping(value = "/diamond/{mobile}", method = RequestMethod.GET)
    @ResponseBody
    public String diamond(@PathVariable String mobile) {
        ResultVo result = new ResultVo();
        try {
            List<Map<String, Object>> list = memberService.diamond(mobile);
            for (Map<String, Object> map : list) {
                map.put("id", map.get("mobile"));
                map.put("text", map.get("name") + "  -  " + map.get("team") + "  -  " + map.get("money"));
                if ((Integer) map.get("level") > 1) {
                    map.put("parent", map.get("parent_mobile"));
                }
                if ((Integer) map.get("team") > 0) {
                    map.put("children", true);
                } else {
                    map.put("icon", "fa fa-user");
                }
            }
            result.put("list", list);
            result.success();
        } catch (Exception e) {
            logger.error("统计查询订单信息出错{}", e);
        }
        return result.toJsonString();
    }

    private String checkTime(String daterangepicker, Map<String, Object> params) {
        if (StringUtil.isBlank(daterangepicker)) {
            daterangepicker = "seven";
        }
        Date start = null;
        Date end = null;
        switch (daterangepicker) {
            case "week":
                start = DateUtil.firstDayOfWeek();
                end = DateUtil.lastDayOfWeek();
                break;
            case "month":
                start = DateUtil.firstDayOfMonth();
                end = DateUtil.lastDayOfMonth();
                break;
            case "seven":
                end = DateUtil.newInstanceDateEnd();
                start = DateUtil.addDays(end, -7);
                break;
            case "thirty":
                end = DateUtil.newInstanceDateEnd();
                start = DateUtil.addDays(end, -30);
                break;
            default:
                String[] dates = daterangepicker.split(" - ");
                if (dates.length == 2) {
                    start = DateUtil.stringToDate(dates[0], DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
                    end = DateUtil.stringToDate(dates[1], DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
                }
        }
        if (StringUtil.isNotBlank(start) && StringUtil.isNotBlank(end)) {
            String startStr = DateUtil.dateToString(start, DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
            String endStr = DateUtil.dateToString(end, DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
            params.put("start", startStr + " 00:00:00");
            params.put("startEnd", startStr + " 23:59:59");
            params.put("end", endStr + " 23:59:59");
            daterangepicker = startStr + " - " + endStr;
        }
        return daterangepicker;
    }
}
