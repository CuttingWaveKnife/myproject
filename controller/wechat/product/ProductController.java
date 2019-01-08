package com.cb.controller.wechat.product;

import com.cb.common.hibernate.query.Page;
import com.cb.common.util.*;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.controller.common.CommonController;
import com.cb.model.active.Record;
import com.cb.model.member.Member;
import com.cb.model.product.Product;
import com.cb.model.product.ProductCategory;
import com.cb.service.activition.BannerService;
import com.cb.service.activition.RecordService;
import com.cb.service.member.MemberService;
import com.cb.service.product.ProductCategoryService;
import com.cb.service.product.ProductService;
import com.cb.vo.ProductCategoryVo;
import com.cb.vo.ProductVo;
import com.cb.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信端产品请求控制器
 */

@Controller("wechat-productController")
@RequestMapping("/wechat/product")
public class ProductController extends CommonController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private RecordService recordService;
    @Autowired
    private MemberService memberService;

    /**
     * 请求产品列表页面
     *
     * @return 产品列表页面
     */
    @RequestMapping(value = "/list")
    public String list(Model model) {
        model.addAttribute("first", ShiroSecurityUtil.getCurrentMember().getAgent() == null);
        model.addAttribute("bannerPage", bannerService.findOnLine());
        model.addAttribute("members", memberService.findVerifyList());
        return "/wechat/product/list";
    }

    /**
     * 请求商品分类
     *
     * @return 返回商品分类
     */
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    @ResponseBody
    public String category() {
        ResultVo result = new ResultVo();
        List<ProductCategory> categories = productCategoryService.findAll();
        result.success();
        result.put("categories", ListUtil.copyPropertiesInList(ProductCategoryVo.class, categories));
        return result.toJsonString();
    }

    /**
     * 请求按条件分页查询产品
     *
     * @param code 产品分类编码
     * @return 返回查询结果集
     */
    @RequestMapping(value = "/find")
    @ResponseBody
    public String find(String code, Integer pageNo, Integer pageSize) {
        ResultVo result = new ResultVo();
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? 10 : pageSize;
        Page<Product> page = new Page<>(pageNo, pageSize);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(code)) {
            params.put("categoryCode", code.split(","));
        }
        try {
            Member member = ShiroSecurityUtil.getCurrentMember();
            page = productService.findPageByParams(page, params);
            for (Product product : page.getResult()) {
                product.setPrice(product.getPriceByLevel(member.getLevel()));
            }
            Page<ProductVo> voPage = new Page<>();
            BeanUtil.copyPropertiesWithoutNullValues(voPage, page);
            List<ProductVo> list = ListUtil.copyPropertiesInList(ProductVo.class, page.getResult());
            voPage.setResult(list);
            result.success();
            result.put("page", voPage);
            result.put("level", member.getLevel());
        } catch (Exception e) {
            logger.error("查询产品列表出错{}", e);
        }

        return result.toJsonString();
    }

    /**
     * 请求获得对应编码的产品详情页面
     *
     * @param code  产品编码
     * @param model 数据保存模型
     * @return 产品详情页面
     */
    @RequestMapping(value = "/detail/{code}")
    public String detail(@PathVariable String code, Model model) {
        Member member = ShiroSecurityUtil.getCurrentMember();
        Member.LevelEnum levelEnum = member.getLevel();
        if (StringUtil.isNotBlank(code)) {
            Product product = productService.findByCode(code);
            if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_START_B), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                    DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END_B), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                    ) {
                if (PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE).equals(code)) {//属于活动商品
                    Record record = recordService.isExist(member.getId());
                    if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {//参与了活动
                        model.addAttribute("limitNumber", record.getNumber());
                    } else {
                        model.addAttribute("limitNumber", -1);
                    }
                }
            }
            model.addAttribute("productCurrent", product.getPrice());
            product.setPrice(product.getPriceByLevel(levelEnum));
            model.addAttribute("product", product);
            model.addAttribute("first", member.getFirst());
            model.addAttribute("score", member.getAvailableScore());
            model.addAttribute("level", levelEnum);
        }
        return "/wechat/product/detail";
    }

    /**
     * 请求获得对应产品编号的产品集合
     *
     * @param strs 产品编号
     * @return 产品集合
     */
    @RequestMapping(value = "/find/{strs}")
    @ResponseBody
    public String findByIds(@PathVariable String strs) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(strs)) {
            String[] temps = strs.split(",");
            List<Product> products = productService.findByCodes(StringUtil.split(strs.replaceAll(":[0-9]+", ""), ","));
            Member member = ShiroSecurityUtil.getCurrentMember();
            if (member != null) {
                Member.LevelEnum level = member.getLevel();
                for (Product product : products) {
                    product.setPrice(product.getPriceByLevel(level));
                }
                result.put("first", member.getFirst());
                result.put("score", member.getAvailableScore());

                BigDecimal amount = BigDecimal.ZERO;
                Double weight = 0d;
                for (Product product : products) {
                    for (String temp : temps) {
                        if (temp.contains(product.getCode())) {
                            Integer quantity = Integer.valueOf(StringUtil.substringAfter(temp, ":"));
                            BigDecimal unitPrice = product.getPriceByLevel(member.getLevel());  //获得对应会员用户的产品单价
                            amount = amount.add(unitPrice.multiply(new BigDecimal(quantity)));  //计算产品的金额
                            weight += new BigDecimal(product.getGrossWeight()).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(quantity)).doubleValue();
                            break;
                        }
                    }
                }

                //邮费计算
                BigDecimal postage;
                if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.POSTAGE_TIME), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) < 0) {
                    postage = BigDecimal.ZERO;
                    if (amount.compareTo(new BigDecimal(PropertiesUtil.getPropertiesValue(Constants.PROPERTY_FREE))) == -1) {
                        postage = new BigDecimal(PropertiesUtil.getPropertiesValue(Constants.PROPERTY_POSTAGE));
                    }
                } else {
                    weight = new BigDecimal(1.2).setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(weight)).setScale(3, RoundingMode.HALF_UP).doubleValue();
                    if (weight > 0) {
                        if (weight <= 2) {
                            postage = new BigDecimal(6);
                        } else {
                            postage = new BigDecimal(6).add(new BigDecimal(Math.ceil(weight - 2) * 2));
                        }
                        result.put("weight", weight);
                    } else {
                        postage = new BigDecimal(PropertiesUtil.getPropertiesValue(Constants.PROPERTY_POSTAGE));
                    }
                    /*//邮费减免
                    if (amount.compareTo(new BigDecimal(PropertiesUtil.getPropertiesValue(Constants.PROPERTY_FREE))) != -1) {
                        postage = BigDecimal.ZERO;
                    }*/
                }
                result.put("postage", postage);
                result.put("amount", amount);
            }
            List<ProductVo> vos = ListUtil.copyPropertiesInList(ProductVo.class, products);
            result.put("products", vos);
            result.success();
        } else {
            result.setMessage("商品编码不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 请求查询对应产品库存
     *
     * @param code 产品编号
     * @return 返回查询结果
     */
    @RequestMapping(value = "/stock/{code}")
    @ResponseBody
    public String findStock(@PathVariable String code) {
        ResultVo result = new ResultVo();
        if (StringUtil.isNotBlank(code)) {
            Product product = productService.findByCode(code);
            if (product != null) {
                result.success();
                /*result.put("stockNum", product.getStockNum());
                result.put("stockStatus", product.getStockStatus());*/
            } else {
                result.setMessage("商品不存在");
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return result.toJsonString();
    }

    /**
     * 活动 期间 banner进入 一期活动
     *
     * @return
     */
    @RequestMapping(value = "/lipstick", method = RequestMethod.GET)
    public String lipstick(Model model) {
        Member member = ShiroSecurityUtil.getCurrentMember();
        Member.LevelEnum levelEnum = member.getLevel();
        String code = PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE);
        if (StringUtil.isNotBlank(code)) {
            Product product = productService.findByCode(code);
            if (product != null) {
                if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                        DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                        ) {
                    if (PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE).equals(code)) {//属于活动商品
                        Record record = recordService.isExist(member.getId());
                        if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {//参与了活动
                            model.addAttribute("limitNumber", record.getNumber());
                        } else {
                            model.addAttribute("limitNumber", -1);
                        }
                    }
                }
                model.addAttribute("productCurrent", product.getPrice());
                product.setPrice(product.getPriceByLevel(levelEnum));
                model.addAttribute("product", product);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long timeEnd = 0;
                //得到毫秒数
                try {
                    timeEnd = sdf.parse(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END)).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//            DateUtil.parse(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END_B), DateUtil.DateFormatter.FORMAT_DD_MM_YYYY_HH_MM_SS);
                model.addAttribute("endTime", timeEnd);
                model.addAttribute("first", member.getFirst());
                model.addAttribute("score", member.getAvailableScore());
                model.addAttribute("level", levelEnum);
            }
        }
        return "wechat/product/lipstick";
    }
}
