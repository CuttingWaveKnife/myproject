package com.cb.controller.system.warehouse;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.JodUtils;
import com.cb.common.util.StringUtil;
import com.cb.model.product.Product;
import com.cb.model.warehouse.Stock;
import com.cb.model.warehouse.Warehouse;
import com.cb.service.product.ProductService;
import com.cb.service.warehouse.StockService;
import com.cb.service.warehouse.WarehouseService;
import com.cb.vo.ResultVo;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.WarehouseStockResponse;
import com.jd.open.api.sdk.response.ECLP.EclpStockQueryStockResponse;
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
 */
@Controller("system-warehouseController")
@RequestMapping("/system/warehouse")
public class WarehouseController extends CommonController {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    /**
     * 仓库列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String warehouse(Model model) {
        model.addAttribute("status", Warehouse.StatusEnum.values());
        return "system/warehouse/list";
    }

    /**
     * 仓库条件查询
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String find(String search, Warehouse.StatusEnum status, Integer pageNo, Integer pageSize, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? -1 : pageSize;// TODO: 2017/3/3 分页未启用
        Page<Warehouse> page = new Page<>(pageNo, pageSize);
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(search)) {
            params.put("search", search);
        }
        if (status != null) {
            params.put("status", status);
        }
        model.addAttribute("page", warehouseService.findPageByParams(page, params));
        return "/system/warehouse/find";
    }

    /**
     * 获取后台仓库详情
     */
    @RequestMapping(value = "/detail/{code}", method = RequestMethod.GET)
    public String detail(@PathVariable String code, Model model) {
        if (StringUtil.isNotBlank(code)) {
            Warehouse warehouse = warehouseService.findByCode(code);
            model.addAttribute("warehouse", warehouse);
        }
        return "/system/warehouse/detail";
    }

    @RequestMapping(value = "/synchro", method = RequestMethod.POST)
    @ResponseBody
    public String synchro() {
        ResultVo result = new ResultVo();
        JodUtils jodUtils = new JodUtils();
        List<Product> products = productService.findAll();
        try {
            for (Warehouse warehouse : warehouseService.findAll()) {
                EclpStockQueryStockResponse response = jodUtils.getStock("1", null, "1", warehouse.getCode());
                for (WarehouseStockResponse responseWsr : response.getQuerystockResult()) {
                    for (Product product : products) {
                        if (responseWsr.getGoodsNo()[0].equals(product.getJodCode())) {
                            Stock stock = stockService.findByProductandWarehouse(product.getId(), warehouse.getId());
                            if (stock == null) {
                                stock = new Stock();
                                stock.setProduct(product);
                                stock.setWarehouse(warehouse);
                            }
                            int num = responseWsr.getUsableNum()[0];
                            stock.setStockNum(num - stock.getLockNum());
                            //剩余存库为0则设置为无库存
                            if (num == 0) {
                                stock.setStockStatus(Stock.StockStatusEnum.NO);
                            }
                            stockService.save(stock);
                            break;
                        }
                    }
                }
                result.success();
            }
        } catch (Exception e) {
            result.setMessage("同步出错：{0}", e.getMessage());
            logger.error("同步京东库存出错：{}", e);
        }
        return result.toJsonString();
    }
}
