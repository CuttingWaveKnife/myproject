package com.cb.controller.system.warehouse;

import com.cb.common.core.controller.CommonController;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.*;
import com.cb.model.order.Order;
import com.cb.model.warehouse.Warehouse;
import com.cb.service.member.MemberService;
import com.cb.service.order.OrderService;
import com.cb.service.product.ProductService;
import com.cb.service.warehouse.WarehouseService;
import com.cb.vo.ResultVo;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.domain.ECLP.EclpOpenService.WarehouseStockResponse;
import com.jd.open.api.sdk.response.ECLP.EclpStockQueryStockResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/3.
 */
@Controller("system-jodController")
@RequestMapping("/system/warehouse/jod")
public class JodController extends CommonController {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    /**
     * 仓库列表
     */
    @RequestMapping(value = "/list")
    public String warehouse(Model model) {
        model.addAttribute("warehouseEnum", warehouseService.findAll());
        return "system/warehouse/jod/list";
    }

    /**
     * 仓库条件查询
     */
    @RequestMapping(value = "/find")
    public String find(String warehouse, String stockType, String goodsNo, String stockStatus, Integer pageNo, Integer pageSize, Model model) {
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? -1 : pageSize;// TODO: 2017/3/3 分页未启用
        Map<String, Object> params = new HashMap<>();
        Page page = new Page<>(pageNo, pageSize);
        JodUtils jodUtils = new JodUtils();
        try {
            Warehouse wh = warehouseService.findByCodeWithCache(warehouse);
            EclpStockQueryStockResponse response = jodUtils.getStock(stockStatus, goodsNo, stockType, wh.getCode());
            int differ = 20;
            params.put("start", DateUtil.format(DateUtil.addDays(DateUtil.newInstanceDateBegin(), -differ), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
            params.put("end", DateUtil.format(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
            params.put("warehouse", wh.getId());
            Page<Map<String, Object>> pageW = productService.getSalesVolume(page, params);
            for (Map<String, Object> map : pageW.getResult()) {
                for (WarehouseStockResponse responseWsr : response.getQuerystockResult()) {
                    if (responseWsr.getGoodsNo()[0].equals(map.get("jodCode"))) {
                        Integer salesNum = ((BigDecimal) map.get("salesNum")).intValue();
                        Integer avgNum = salesNum / differ;
                        responseWsr.setExt1(new String[]{avgNum.toString()});
                        if (avgNum != 0) {
                            responseWsr.setExt2(new String[]{responseWsr.getUsableNum()[0] / avgNum + ""});
                        }
                        break;
                    }
                }
            }
            response.getQuerystockResult().sort(new Comparator<WarehouseStockResponse>() {
                @Override
                public int compare(WarehouseStockResponse o1, WarehouseStockResponse o2) {
                    String[] array1 = o1.getExt2();
                    String[] array2 = o2.getExt2();
                    String temp1 = null;
                    String temp2 = null;
                    if (array1 != null) {
                        temp1 = array1[0];
                    }
                    if (array2 != null) {
                        temp2 = o2.getExt2()[0];
                    }
                    if (temp1 == null && temp2 != null) {
                        return 1;
                    } else if (temp1 != null && temp2 == null) {
                        return -1;
                    } else if (temp1 == null && temp2 == null) {
                        return 1;
                    } else {
                        return Integer.valueOf(temp1) - Integer.valueOf(temp2);
                    }
                }
            });
            model.addAttribute("response", response);
            model.addAttribute("page", page);
        } catch (JdException e) {
            e.printStackTrace();
        }
        return "/system/warehouse/jod/find";
    }

    /**
     * 导出所有单独订单 类型ALONE
     *
     * @return
     */
    @RequestMapping(value = "/exceloutalone", method = RequestMethod.POST)
    @ResponseBody
    public String exceloutAlone(String datepicker, Integer pageNo, Integer pageSize) {
        ResultVo resultVo = new ResultVo();
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
//                params.put("startTime", temps[0] + " 00:00:00");
//                params.put("endTime", temps[1] + " 23:59:59");
                params.put("financeAuditStartTime", temps[0] + " 00:00:00");
                params.put("financeAuditEndTime", temps[1] + " 23:59:59");
            }
        } else {
            params.put("financeAuditStartTime", DateUtil.dateToString(DateUtil.newInstanceDateBegin(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 00:00:00");
            params.put("financeAuditEndTime", DateUtil.dateToString(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " 23:59:59");
        }
        params.put("type", Order.TypeEnum.ALONE);
        params.put("$OrderBy", " financeAuditDate desc");
        params.put("status", new String[]{"DISTRIBUTION", "TRANSPORTATION", "COMPLETED"});
//        params.put("status", new String[]{"RECEIVEING", "PAYMENTING", "AUDITING", "DISTRIBUTION", "TRANSPORTATION", "COMPLETED", "SELF", "CANCELED", "FINREVOKED", "CUSCANCELED", "FINCANCELED"});
        String df = PropertiesUtil.getPropertiesValue("uploadurl") + "orderAlone.xls";
        File file = new File(df);
        if (file.exists()) {
            file.delete();
        }
        String dd = PropertiesUtil.getPropertiesValue("downurl") + "orderAlone.xls";
        List<Map<String, Object>> listnew = orderService.findMapByParamsAlone(params);
        ExclUtil exclUtil = new ExclUtil();
        try {
            exclUtil.excelOutAlone(listnew, df, orderService);
            resultVo.put("url", dd);
            resultVo.success();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultVo.toJsonString();
    }

}
