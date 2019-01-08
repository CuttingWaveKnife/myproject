package com.cb.controller.wechat.member;

import com.cb.common.core.controller.CommonController;
import com.cb.common.exception.AppServiceException;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.common.AddressDatabase;
import com.cb.model.member.Address;
import com.cb.model.member.Member;
import com.cb.model.warehouse.Warehouse;
import com.cb.service.common.AddressDatabaseService;
import com.cb.service.member.AddressService;
import com.cb.service.member.MemberService;
import com.cb.service.warehouse.WarehouseService;
import com.cb.vo.AddressVo;
import com.cb.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("wechat-addressController")
@RequestMapping("/wechat/member/address")
public class AddressController extends CommonController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressDatabaseService addressDatabaseService;

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 请求获取当前登录会员所有地址列表页面
     *
     * @param model 数据存放模型
     * @return 地址列表页面
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model, boolean flag) {
        Member member = memberService.findById(ShiroSecurityUtil.getCurrentMemberId());
        if (member != null) {
            model.addAttribute("addresses", member.getAddresses());
        }
        if (flag) {
            session.setAttribute("flag", flag);
        }
        return "/wechat/member/address/list";
    }

    /**
     * 请求获取地址详情页面
     *
     * @param id 地址id
     * @return 地址详情页面
     */
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String detail(String id, Model model) {
        if (StringUtil.isNotBlank(id)) {
            model.addAttribute("address", addressService.findById(id));
        }
        return "/wechat/member/address/edit";
    }

    /**
     * 请求保存地址
     *
     * @param address 地址
     * @return 保存结果
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public String save(Address address) {
        ResultVo result = new ResultVo();
        Member member = ShiroSecurityUtil.getCurrentMember();
        if (member != null) {
            try {
                if (address.isDef()) {//是默认地址 修改其他地址非默认
                    for (Address addressno : member.getAddresses()) {
                        addressno.setDef(false);
                    }
                }
                addressService.saveAddress(address);
                result.success();
            } catch (AppServiceException e) {
                logger.error("保存地址出错{}", e.getMessage());
                result.setMessage(e.getMessage());
            } catch (Exception e) {
                logger.error("保存地址出错{}", e);
            }
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }

    /**
     * 请求获取地址
     *
     * @return 返回结果
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public String getAddress(String id) {
        ResultVo result = new ResultVo();
        Member member = memberService.findById(ShiroSecurityUtil.getCurrentMemberId());
        if (member != null) {
            AddressVo addressVo = new AddressVo();
            if (StringUtil.isNotBlank(id)) {
                Address address = addressService.findById(id);
                if (address != null && StringUtil.equals(address.getMember().getId(), member.getId())) {
                    BeanUtil.copyProperties(addressVo, address);
                    result.success();
                } else {
                    result.setMessage("地址不存在");
                }
            } else {
                Address address = member.getAddress();
                if (address != null) {
                    BeanUtil.copyProperties(addressVo, address);
                    result.success();
                } else {
                    result.setMessage("没有默认地址");
                }
            }
            String province = addressVo.getProvince();
            if (StringUtil.isNotBlank(province)) {
                AddressDatabase addressDatabase = addressDatabaseService.findById(province);
                if (addressDatabase != null) {
                    Warehouse warehouse = warehouseService.findByCodeWithCache(addressDatabase.getDeliver());
                    result.put("warehouse", warehouse.getName());
                }
            }
            result.put("address", addressVo);
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }

    /**
     * 操作默认地址
     *
     * @param id 地址id
     * @return
     */
    @RequestMapping(value = "/setdefault", method = RequestMethod.POST)
    @ResponseBody
    public String setdefault(String id) {
        ResultVo result = new ResultVo();
        Member member = memberService.findById(ShiroSecurityUtil.getCurrentMemberId());
        if (member != null && StringUtil.isNotBlank(id)) {
            Address address1 = addressService.findById(id);
            if (!address1.isDef()) {
                //设为默认
                for (Address address : member.getAddresses()) {
                    address.setDef(false);
                }
                address1.setDef(true);
                memberService.save(member);
            }
            result.success();
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }

    /**
     * 删除地址, 至少保留一个默认地址
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String delete(String id) {
        ResultVo result = new ResultVo();
        Member member = ShiroSecurityUtil.getCurrentMember();
        if (member != null) {
            Address address = addressService.findById(id);
            if (address != null) {
                if (address.isDef()) {
                    result.setMessage("默认地址不能删除");
                } else {
                    addressService.delete(address);
                    result.success();
                }
            }
        } else {
            result.setMessage("未登录");
        }
        return result.toJsonString();
    }
}
