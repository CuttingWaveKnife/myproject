package com.cb.service.activition.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.DateUtil;
import com.cb.common.util.StringUtil;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.dao.active.BannerDao;
import com.cb.model.active.Banner;
import com.cb.service.activition.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/21.
 */
@Service
@Transactional
public class BannerServiceImpl extends CommonServiceImpl<Banner, String> implements BannerService {

    @Autowired
    private BannerDao bannerDao;

    @Override
    protected CommonDao<Banner, String> getCommonDao() {
        return bannerDao;
    }

    @Override
    public Page<Banner> findPageByBanner(Page<Banner> page, Map<String, Object> param) {
        return bannerDao.findPageByBanner(page, param);
    }

    @Override
    public void edit(Banner banner) {
        //编辑已存在的头图
        if (StringUtil.isNotBlank(banner.getId())) {
            Banner dbBanner = bannerDao.get(banner.getId());
            BeanUtil.copyPropertiesWithoutNullValues(dbBanner, banner);
            save(dbBanner);
        }
        //新增头图
        else {
            banner.setStatus(Banner.StatusEnum.ON);//默认上架,因为有编辑时效
            banner.getImage().setForeignId(banner.getId());
            save(banner);
        }
    }

    @Override
    public Page<Banner> findOnLine() {
        Page<Banner> page = new Page<>(1, 20);
        Map<String, Object> params = new HashMap<>();
        params.put("status", Banner.StatusEnum.ON);
        params.put("time", DateUtil.newInstanceDate());
        params.put("$orderBy", "ab.sort");
        return bannerDao.findOnLine(page, params);
    }
}
