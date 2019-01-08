package com.cb.controller.system.security;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.StringUtil;
import com.cb.model.member.Member;
import com.cb.model.security.User;
import com.cb.service.security.RoleService;
import com.cb.service.security.UserService;
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
 * Created by l on 2016/11/22.
 */
@Controller("sys-userController")
@RequestMapping("/system/user")
public class UserController extends CommonController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("roles", roleService.findAll());
        return "/system/user/list";
    }

    @RequestMapping(value = "/find", method = RequestMethod.POST)
    public String find(String search, String role, Integer pageNo, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        Page<User> page = new Page<>(pageNo, 10);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("search", search);
        }
        if (StringUtil.isNotBlank(role)) {
            params.put("r.role", role);
        }
        page = userService.findPageByParams(page, params);
        model.addAttribute("page", page);
        return "/system/user/find";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String edit(String id, Model model) {
        return "/system/user/edit";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public String save(Member member) {
        return "";
    }

    @RequestMapping("/detail/{id}")
    public String detail(@PathVariable String id, Model model) {
        return "/system/user/detail";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String delete(String ids) {
        return "/system/user/detail";
    }
}
