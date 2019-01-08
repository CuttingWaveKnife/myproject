package com.cb.service.lottery;

import com.cb.common.core.service.CommonService;
import com.cb.model.lottery.Lottery;

import java.util.List;
import java.util.Map;

public interface LotteryService extends CommonService<Lottery, String> {

    List<Map<String, Object>> list();
}
