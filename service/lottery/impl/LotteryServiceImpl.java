package com.cb.service.lottery.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.dao.lottery.LotteryDao;
import com.cb.model.lottery.Lottery;
import com.cb.service.lottery.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LotteryServiceImpl extends CommonServiceImpl<Lottery, String> implements LotteryService {

    @Autowired
    private LotteryDao lotteryDao;

    @Override
    protected CommonDao<Lottery, String> getCommonDao() {
        return lotteryDao;
    }

    @Override
    public List<Map<String, Object>> list() {
        return lotteryDao.list();
    }
}
