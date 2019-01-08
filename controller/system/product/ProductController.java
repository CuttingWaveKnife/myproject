package com.cb.controller.system.product;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.JsonUtil;
import com.cb.common.util.ListUtil;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.member.Member;
import com.cb.model.order.Order;
import com.cb.model.product.Product;
import com.cb.model.product.ProductCategory;
import com.cb.model.security.User;
import com.cb.model.warehouse.Stock;
import com.cb.model.warehouse.Warehouse;
import com.cb.service.product.ProductCategoryService;
import com.cb.service.product.ProductService;
import com.cb.service.warehouse.WarehouseService;
import com.cb.vo.ProductCategoryVo;
import com.cb.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/3.
 * 后台产品请求控制器
 */
@Controller("system-productController")
@RequestMapping("/system/product")
public class ProductController extends CommonController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 请求产品列表页面
     *
     * @return 产品列表页面
     */
    @RequestMapping(value = "/list")
    public String list(Model model) {
        model.addAttribute("categorys", productCategoryService.findAll());
        model.addAttribute("productStatus", Product.StatusEnum.values());
        return "/system/product/list";
    }

    /**
     * 请求按条件分页查询产品
     *
     * @return 返回查询结果集
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String find(String search, Product.StatusEnum status, String datepicker, String categoryCode, Integer pageNo, Integer pageSize, Model model) {
        ResultVo result = new ResultVo();
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? 10 : pageSize;
        Page<Product> page = new Page<>(pageNo, pageSize);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("code", "%"+search+"%");
            params.put("name", "%"+search+"%");
        }
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                params.put("auditStartTime", temps[0]);
                params.put("auditEndTime", temps[1]);
            }
        }
        if (StringUtil.isNotBlank(status)) {
            params.put("status", status.ordinal());
        }
        if (StringUtil.isNotBlank(categoryCode)) {
            params.put("categoryCode", categoryCode);
        }
        try {
            page = productService.findPageByBackstage(page, params);
            result.success();
            model.addAttribute("page", page);
        } catch (Exception e) {
            logger.error("查询产品列表出错{}", e);
        }
        return "/system/product/find";
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
     * 请求商品上下架
     *
     * @return 返回商品分类
     */
    @RequestMapping(value = "/shelves/{code}", method = RequestMethod.POST)
    @ResponseBody
    public String shelves(@PathVariable String code) {
        ResultVo result = new ResultVo();
        Product product = productService.findByCode(code);
        if (product != null) {
            if (Product.StatusEnum.ON_THE_SHELVES.equals(product.getStatus())) {
                product.setStatus(Product.StatusEnum.OFF_THE_SHELVES);
                result.setMessage("已成功下架");
            } else {
                product.setStatus(Product.StatusEnum.ON_THE_SHELVES);
                result.setMessage("已成功上架");
            }
            productService.update(product);
            result.setSuccess(true);
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
        User user = ShiroSecurityUtil.getCurrentUser();
        if (user != null) {
            if (StringUtil.isNotBlank(code)) {
                Product product = productService.findByCode(code);
                model.addAttribute("productCurrent", product.getPrice());
                model.addAttribute("product", product);
            }
        }
        return "/system/product/detail";
    }

    /**
     * 商品编辑
     *
     * @return
     */
    @RequestMapping(value = "/edit")
    public String edit(String id, Model model) {
        Product product = productService.findById(id);
        if (product != null) {
            model.addAttribute("product", product);
        }
        model.addAttribute("categorys", productCategoryService.findAll());
        model.addAttribute("levels", Member.LevelEnum.values());
        model.addAttribute("stockStatuses", Stock.StockStatusEnum.values());
        model.addAttribute("wareHouses", warehouseService.findAll());
        return "/system/product/edit";
    }

    /**
     * 请求保存产品
     *
     * @return 返回保存结果
     */
    @RequestMapping("/save")
    @ResponseBody
    public String save(Product product) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        try {
            productService.edit(product);
            result.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonUtil.toFullJson(result);
    }

}
