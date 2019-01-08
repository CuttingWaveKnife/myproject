package com.cb.service.member;

import com.cb.common.core.service.CommonService;
import com.cb.model.member.Address;

/**
 * Created by l on 2016/11/28.
 */
public interface AddressService extends CommonService<Address, String> {

    /**
     * 保存地址
     *
     * @param address
     */
    void saveAddress(Address address);

    /**
     * 删除地址 如果是默认取消默认
     * @param id
     */
    void deleteById(String id);
}
