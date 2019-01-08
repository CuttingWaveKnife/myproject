package com.cb.dao.lottery;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.util.Constants;
import com.cb.common.util.PropertiesUtil;
import com.cb.model.lottery.Lottery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class LotteryDao extends CommonDao<Lottery, String> {

    public List<Map<String, Object>> list() {
        String sql = "SELECT oo.code, oo.name, oo.mobile, oo.province_name province, oo.city_name city, oo.area_name area, oo.detail FROM o_order oo WHERE NOT EXISTS(SELECT 1 FROM l_lottery ll WHERE oo.mobile = ll.mobile) AND oo.type = 'ALONE' AND oo.status IN('DISTRIBUTION','TRANSPORTATION','COMPLETED') AND oo.deleted=FALSE AND oo.finance_date BETWEEN ? AND ? GROUP BY oo.mobile";
        return (List<Map<String, Object>>) queryForMap(sql, PropertiesUtil.getPropertiesValue(Constants.LOTTERY_START), PropertiesUtil.getPropertiesValue(Constants.LOTTERY_END));
//        return (List<Map<String, Object>>) queryForMap(sql, "2017-05-15 00:00:00", "2017-05-15 23:59:59");
    }
}
