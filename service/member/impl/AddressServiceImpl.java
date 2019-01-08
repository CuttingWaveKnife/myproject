package com.cb.service.member.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.exception.AppServiceException;
import com.cb.common.util.ShiroSecurityUtil;
import com.cb.common.util.StringUtil;
import com.cb.dao.member.AddressDao;
import com.cb.model.member.Address;
import com.cb.service.member.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by l on 2016/11/28.
 */
@Service
@Transactional
public class AddressServiceImpl extends CommonServiceImpl<Address, String> implements AddressService {

    @Autowired
    private AddressDao addressDao;

    @Override
    protected CommonDao<Address, String> getCommonDao() {
        return addressDao;
    }

    @Override
    public void saveAddress(Address address) {
        if (StringUtil.isNotBlank(address.getId())) {
            Address dbAddress = findById(address.getId());
            if (dbAddress != null) {
                dbAddress.setName(address.getName());
                dbAddress.setMobile(address.getMobile());
                dbAddress.setProvince(address.getProvince());
                dbAddress.setProvinceName(address.getProvinceName());
                dbAddress.setCity(address.getCity());
                dbAddress.setCityName(address.getCityName());
                dbAddress.setArea(address.getArea());
                dbAddress.setAreaName(address.getAreaName());
                dbAddress.setDetail(address.getDetail());
                dbAddress.setFull(address.getFull());
                save(dbAddress);
            } else {
                throw new AppServiceException("地址不存在");
            }
        } else {
            address.setMember(ShiroSecurityUtil.getCurrentMember());
            save(address);
        }
    }

    @Override
    public void deleteById(String id) {
        Address address = findById(id);
        address.setDef(false);
        address.setDeleted(true);
    }
}
