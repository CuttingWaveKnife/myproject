package com.cb.service.warehouse;

import com.cb.common.core.service.CommonService;
import com.cb.model.warehouse.Stock;
import com.cb.vo.ResultVo;

public interface StockService extends CommonService<Stock, String> {

    /**
     * 根据产品id和仓库id查找库存
     *
     * @param productId   产品id
     * @param warehouseId 仓库id
     * @return 库存信息
     */
    Stock findByProductandWarehouse(String productId, String warehouseId);

    /**
     * 检查是否有库存
     *
     * @param stock    库存
     * @param quantity 数量
     * @return 结果
     */
    ResultVo check(Stock stock, Integer quantity);

    /**
     * 下单时保存
     *
     * @param stock    库存
     * @param quantity 数量
     */
    void saveWithSale(Stock stock, Integer quantity);

    /**
     * 取消订单时保存
     *
     * @param stock    库存
     * @param quantity 数量
     */
    void saveWithCancel(Stock stock, Integer quantity);

    /**
     * 发货时保存
     *
     * @param stock    库存
     * @param quantity 数量
     */
    void saveWithDeliver(Stock stock, Integer quantity);
}
