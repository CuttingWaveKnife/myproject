package com.cb.controller.system.cache;

import com.cb.common.core.controller.CommonController;
import com.cb.common.spring.SpringContextHolder;
import com.cb.common.util.Constants;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.vo.ResultVo;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 微信端订单控制器
 */
@Controller("system-cacheController")
@RequestMapping("/system/cache")
public class CacheController extends CommonController {

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index() {
        return "/system/cache/index";
    }

    @RequestMapping(value = "/clear-mycache", method = RequestMethod.POST)
    @ResponseBody
    public String clearMycache() {
        ResultVo result = new ResultVo();
        try {
            CacheManager cacheManager = SpringContextHolder.getBean("cacheManager");
            Cache cache = cacheManager.getCache(Constants.CACHE_MYCACHE);
            cache.clear();
            result.success();
        } catch (Exception e) {
            logger.error("清除mycache缓存出错:{}", e);
        }
        return result.toJsonString();
    }

    @RequestMapping(value = "/update-permission", method = RequestMethod.POST)
    @ResponseBody
    public String updatePermission() {
        ResultVo result = new ResultVo();
        try {
            ShiroSecurityUtil.updatePermission();
            result.success();
        } catch (Exception e) {
            logger.error("更新shiro权限出错:{}", e);
        }
        return result.toJsonString();
    }
}
