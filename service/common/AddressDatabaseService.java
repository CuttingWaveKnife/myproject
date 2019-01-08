package com.cb.service.common;

import com.cb.common.core.service.CommonService;
import com.cb.model.common.AddressDatabase;
import com.cb.vo.AddressDatabaseVo;

import java.util.List;

/**
 * Created by l on 2016/11/28.
 */
public interface AddressDatabaseService extends CommonService<AddressDatabase, String> {

    /**
     * 查询所有省份
     *
     * @return 返回所有省份
     */
    List<AddressDatabaseVo> findAllProvinceVo();

    /**
     * 根据地址id查询其下子集合地址
     *
     * @param addressId 地址id
     * @return 子集合地址
     */
    List<AddressDatabaseVo> findAllChildrenVo(String addressId);
}
