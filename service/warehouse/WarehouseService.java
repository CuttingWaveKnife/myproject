package com.cb.service.warehouse;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.warehouse.Warehouse;

import java.util.Map;

public interface WarehouseService extends CommonService<Warehouse, String> {

    /**
     * 根据分类编码查询仓库
     *
     * @param code 分类编码
     * @return 仓库
     */
    Warehouse findByCode(String code);

    /**
     * 根据分类编码查询仓库(有缓存)</br>
     * <p style='color:red'>此方法不支持hibernate级联查询</p>
     *
     * @param code 分类编码
     * @return 仓库
     */
    Warehouse findByCodeWithCache(String code);

    /**
     * 通过条件分页查询仓库
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return 返回结果集
     */
    Page<Warehouse> findPageByParams(Page<Warehouse> page, Map<String, Object> params);
}
