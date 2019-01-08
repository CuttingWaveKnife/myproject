package com.cb.service.product;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.product.Product;
import com.cb.vo.ResultVo;

import java.util.List;
import java.util.Map;

public interface ProductService extends CommonService<Product, String> {

    /**
     * 根据产品编码查询产品
     *
     * @param code 产品编码
     * @return 产品
     */
    Product findByCode(String code);

    /**
     * 根据产品编码查询产品集合
     *
     * @param codes 产品编码
     * @return 产品集合
     */
    List<Product> findByCodes(String[] codes);

    /**
     * 根据分页和查询条件进行分页查询产品
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 分页查询结果集
     */
    Page<Product> findPageByParams(Page<Product> page, Map<String, Object> params);

    /**
     * 后台根据分页和查询条件进行分页查询产品
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 分页查询结果集
     */
    Page<Product> findPageByBackstage(Page<Product> page, Map<String, Object> params);

    /**
     * 校验产品是否符合销售标准
     *
     * @param product  销售的产品
     * @param quantity 销售的数量
     * @return 校验结果
     */
    ResultVo checkSale(Product product, Integer quantity);

    /**
     * 售出产品后更新产品数据
     *
     * @param product  售出的产品
     * @param quantity 销售的数量
     */
    void saveProductWithSale(Product product, Integer quantity);

    /**
     * 客户取消订单后恢复产品数据
     *
     * @param product  产品
     * @param quantity 恢复的数量
     */
    void saveProductWithCancel(Product product, Integer quantity);

    /**
     * 查询销售量
     *
     * @return 返回查询结果集
     */
    Page<Map<String, Object>> getSalesVolume(Page page, Map<String, Object> params);

    /**
     * 编辑产品
     * @return 返回查询结果集
     */
    void edit(Product product);
}
