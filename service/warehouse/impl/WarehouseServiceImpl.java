package com.cb.service.warehouse.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.Constants;
import com.cb.dao.warehouse.WarehouseDao;
import com.cb.model.warehouse.Warehouse;
import com.cb.service.warehouse.WarehouseService;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class WarehouseServiceImpl extends CommonServiceImpl<Warehouse, String> implements WarehouseService {

    @Autowired
    private WarehouseDao warehouseDao;

    @Override
    protected CommonDao<Warehouse, String> getCommonDao() {
        return warehouseDao;
    }

    @Override
    @Cacheable(value = Constants.CACHE_MYCACHE, key = "#root.method.name")
    public List<Warehouse> findAll() {
        return warehouseDao.find(Restrictions.eq("status", Warehouse.StatusEnum.NORMAL));
    }

    @Override
    public Warehouse findByCode(String code) {
        return warehouseDao.findUnique(Restrictions.eq("code", code));
    }

    @Override
    @Cacheable(value = Constants.CACHE_MYCACHE, key = "#root.method.name+#code")
    public Warehouse findByCodeWithCache(String code) {
        return warehouseDao.findUnique(Restrictions.eq("code", code));
    }

    @Override
    public Page<Warehouse> findPageByParams(Page<Warehouse> page, Map<String, Object> params) {
        return warehouseDao.findPageByParams(page, params);
    }
}
