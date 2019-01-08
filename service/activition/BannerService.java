package com.cb.service.activition;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.active.Banner;

import java.util.Map;

/**
 * Created by GuoMIn on 2017/3/21.
 */
public interface BannerService extends CommonService<Banner, String> {


    Page<Banner> findPageByBanner(Page<Banner> page, Map<String, Object> param);


    /**
     * 编辑
     *
     * @return 返回查询结果集
     */
    void edit(Banner banner);

    /**
     * 查询规定时间 可用的
     *
     * @return
     */
    Page<Banner> findOnLine();
}
