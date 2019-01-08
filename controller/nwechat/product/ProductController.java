package com.cb.controller.nwechat.product;

import com.cb.common.hibernate.query.Page;
import com.cb.common.util.*;
import com.cb.controller.common.CommonController;
import com.cb.model.active.Banner;
import com.cb.model.active.Record;
import com.cb.model.member.Member;
import com.cb.model.product.Product;
import com.cb.model.product.ProductCategory;
import com.cb.service.activition.BannerService;
import com.cb.service.activition.RecordService;
import com.cb.service.product.ProductCategoryService;
import com.cb.service.product.ProductService;
import com.cb.vo.PageVo;
import com.cb.vo.ResultVo;
import com.cb.vo.active.BannerVo;
import com.cb.vo.member.MemberVo;
import com.cb.vo.product.ProductCategoryVo;
import com.cb.vo.product.ProductListVo;
import com.cb.vo.product.ProductVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("nwechat-productController")
@RequestMapping("/nwechat/product")
public class ProductController extends CommonController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private RecordService recordService;


    /**
     * 产品列表页面
     *
     * @return 产品列表页面
     */
    @RequestMapping(value = "/banner")
    @ResponseBody
    public String list(Model model) {
        ResultVo resultVo = new ResultVo();
        Page<Banner> page = bannerService.findOnLine();
        PageVo<BannerVo> voPage = BannerVo.toPageVo(page);

        resultVo.put("first", ShiroSecurityUtil.getCurrentMember().getAgent() == null);
        resultVo.put("bannerVoList", voPage.getList());
        resultVo.success();
        return Json.toJson(resultVo);
    }

    /**
     * 商品分类
     *
     * @return
     */
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    @ResponseBody
    public String category() {
        ResultVo result = new ResultVo();
        List<ProductCategory> categories = productCategoryService.findAll();
        result.success();
        result.put("productCategoryVoList", ListUtil.copyPropertiesInList(ProductCategoryVo.class, categories));
        return Json.toJson(result);
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
            PageVo<ProductListVo> pageVo = ProductListVo.toVoPage(page);
            result.success();
            result.put("ProductVoPage", pageVo);
            result.put("level", member.getLevel());
        } catch (Exception e) {
            logger.error("查询产品列表出错{}", e);
        }
        return Json.toJson(result);
    }

    @RequestMapping(value = "/detail/{code}", method = RequestMethod.GET)
    public
    @ResponseBody
    String detail(@PathVariable String code, Model model) {
        ResultVo result = new ResultVo();
        if (StringUtil.isBlank(code)) {
            result.setSuccess(false);
            return Json.toJson(result);
        }
        Member member = ShiroSecurityUtil.getCurrentMember();
        Member.LevelEnum levelEnum = member.getLevel();

        Product product = productService.findByCode(code);
        ProductVo productVo = ProductVo.toProductVo(product);
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_START_B), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END_B), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                ) {
            if (PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE).equals(code)) {//属于活动商品
                Record record = recordService.isExist(member.getId());
                if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {//参与了活动
                    productVo.setLimitNumber(record.getNumber());
                } else {
                    productVo.setLimitNumber(-1);
                }
            }
        }
        productVo.setProductCurrent(product.getPrice());
        productVo.setPrice(product.getPriceByLevel(levelEnum));
        MemberVo memberVo = MemberVo.toMemberVo(member);
        result.put("productVo", productVo);
        result.put("memberVo", memberVo);
        result.success();
        return Json.toJson(result);
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
            } else {
                result.setMessage("商品不存在");
            }
        } else {
            result.setMessage("参数不能为空");
        }
        return Json.toJson(result);
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
    
}
