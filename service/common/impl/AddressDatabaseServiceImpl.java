package com.cb.service.common.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.util.Constants;
import com.cb.common.util.ListUtil;
import com.cb.common.util.StringUtil;
import com.cb.dao.common.AddressDatabaseDao;
import com.cb.model.common.AddressDatabase;
import com.cb.service.common.AddressDatabaseService;
import com.cb.vo.AddressDatabaseVo;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by l on 2016/11/28.
 */
@Service
@Transactional
public class AddressDatabaseServiceImpl extends CommonServiceImpl<AddressDatabase, String> implements AddressDatabaseService {

    @Autowired
    private AddressDatabaseDao addressDatabaseDao;

    @Override
    protected CommonDao<AddressDatabase, String> getCommonDao() {
        return addressDatabaseDao;
    }

    @Override
    @Cacheable(value = Constants.CACHE_MYCACHE, key = "#root.method.name")
    public List<AddressDatabaseVo> findAllProvinceVo() {
        List<AddressDatabase> provinces = addressDatabaseDao.find(Restrictions.eq("level", AddressDatabase.LevelEnum.PROVINCE));
        return ListUtil.copyPropertiesInList(AddressDatabaseVo.class, provinces);
    }

    @Override
    @Cacheable(value = Constants.CACHE_MYCACHE, key = "#root.method.name+#addressId")
    public List<AddressDatabaseVo> findAllChildrenVo(String addressId) {
        if (StringUtil.isNotBlank(addressId)) {
            AddressDatabase addressDatabase = findById(addressId);
            if (addressDatabase != null) {
                return ListUtil.copyPropertiesInList(AddressDatabaseVo.class, addressDatabase.getChildren());
            }
        }
        return null;
    }
}
