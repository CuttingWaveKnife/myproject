package com.cb.controller.system.product;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.*;
import com.cb.model.active.Banner;
import com.cb.model.active.Record;
import com.cb.service.activition.BannerService;
import com.cb.service.activition.RecordService;
import com.cb.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/22.
 */
@Controller("system-bannerController")
@RequestMapping("/system/banner")
public class BannerController extends CommonController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private RecordService recordService;

    @RequestMapping(value = "/list")
    public String list(Model model) {
        model.addAttribute("statuses", Banner.StatusEnum.values());
        return "/system/banner/list";
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String find(String search, String remark, Banner.StatusEnum status, String datepicker, Integer pageNo, Integer pageSize, Model model) {
        ResultVo result = new ResultVo();
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? 10 : pageSize;
        Page<Banner> page = new Page<>(pageNo, pageSize);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("startTime", temps[0]);
                params.put("endTime", temps[1]);
            }
        }
        if (StringUtil.isNotBlank(search)) {
            params.put("remark", "%" + search + "%");
            params.put("title", "%" + search + "%");
        }
        if (StringUtil.isNotBlank(status)) {
            params.put("status", status.ordinal());
        }
        params.put("$orderBy", "ab.creationTime desc");
        try {
            page = bannerService.findPageByBanner(page, params);
            result.success();
            model.addAttribute("page", page);
        } catch (Exception e) {
            logger.error("查询产品列表出错{}", e);
        }
        return "/system/banner/find";
    }

    /**
     * 删除广告
     *
     * @return
     */
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String delete(@PathVariable String id) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(id)) {
            Banner banner = bannerService.findById(id);
            if (banner != null) {
                bannerService.delete(banner);
                result.success();
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    @RequestMapping(value = "/details/${id}", method = RequestMethod.GET)
    public String details(@PathVariable String id, Model model) {
        if (StringUtil.isNotBlank(id)) {
            Banner banner = bannerService.findById(id);
            model.addAttribute("banner", banner);
        }
        return "system/banner/details";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String edit(String id, Model model) {
        if (StringUtil.isNotBlank(id)) {
            Banner banner = bannerService.findById(id);
            model.addAttribute("banner", banner);
        }
        return "system/banner/edit";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public String save(Banner banner) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        try {
            bannerService.edit(banner);
            result.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonUtil.toFullJson(result);
    }


    /**
     * 请求商品分类
     *
     * @return 返回商品分类
     */
    @RequestMapping(value = "/shelves/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String shelves(@PathVariable String id) {
        ResultVo result = new ResultVo();
        Banner banner = bannerService.findById(id);
        if (banner != null) {
            if (Banner.StatusEnum.ON.equals(banner.getStatus())) {
                banner.setStatus(Banner.StatusEnum.OFF);
                result.setMessage("已成功下架");
            } else {
                banner.setStatus(Banner.StatusEnum.ON);
                result.setMessage("已成功上架");
            }
            bannerService.update(banner);
            result.setSuccess(true);
        }
        return result.toJsonString();
    }

    @RequestMapping(value = "/lipstick/list", method = RequestMethod.GET)
    public String lipstickList(Model model) {
        return "system/banner/lipstick/list";
    }

    @RequestMapping(value = "/lipstick/find", method = RequestMethod.GET)
    public String lipstickFind(Model model, String status, Integer pageNo, Integer pageSize, String search) {
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? 10 : pageSize;
        Page<Map<String, Object>> page = new Page<>(pageNo, pageSize);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("name", "%" + search + "%");
            params.put("mobile", "%" + search + "%");
        }
        if (StringUtil.isNotBlank(status)) {
            params.put("status", status);
        } else {
            params.put("status", Record.StatusEnum.YES.ordinal());
        }
        params.put("audit_user_id", ShiroSecurityUtil.getCurrentUserId());
        boolean[] booleans = ShiroSecurityUtil.getSubject().hasRoles(ListUtil.arrayToList(new String[]{"super", "management"}));
        for (boolean aBoolean : booleans) {
            if (aBoolean) {
                params.remove("audit_user_id");
            }
        }
        page = recordService.findPageByParams(page, params);
        model.addAttribute("page", page);
        return "system/banner/lipstick/find";
    }

    @RequestMapping(value = "/lipstick/save", method = RequestMethod.POST)
    @ResponseBody
    public String lipstickSave(String id, Integer praiseNumber) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(id)) {
            Record record = recordService.findById(id);
            record.setNumber(praiseNumber / 2);
            record.setPraiseNumber(praiseNumber);
            record.setAuditDate(DateUtil.newInstanceDate());
            recordService.save(record);
            result.success();
        }
        return result.toJsonString();
    }

}
