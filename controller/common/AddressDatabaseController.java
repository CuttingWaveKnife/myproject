package com.cb.controller.common;

import com.cb.common.util.JsonUtil;
import com.cb.common.util.StringUtil;
import com.cb.service.common.AddressDatabaseService;
import com.cb.vo.AddressDatabaseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/common/address")
public class AddressDatabaseController {

    @Autowired
    private AddressDatabaseService addressDatabaseService;

    /**
     * 请求获取地址库信息
     *
     * @param aid 父级地址id
     * @return 返回地址库信息
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public String findAddress(String aid) {
        List<AddressDatabaseVo> list;
        //当父级地址id为空时，则查询所有省份地址信息
        if (StringUtil.isNotBlank(aid)) {
            list = addressDatabaseService.findAllChildrenVo(aid);
        } else {
            list = addressDatabaseService.findAllProvinceVo();
        }
        return JsonUtil.toJson(list);
    }
}
