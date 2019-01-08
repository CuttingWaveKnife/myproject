package com.cb.controller.system.lottery;

import com.cb.controller.common.CommonController;
import com.cb.model.lottery.Lottery;
import com.cb.service.lottery.LotteryService;
import com.cb.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.cb.common.shiro.ChainDefinitionSectionMetaSource.i;

/**
 * @author yangjin 2017/5/17
 */
@Controller
@RequestMapping("/system/lottery")
public class LotteryController extends CommonController {

    @Autowired
    private LotteryService lotteryService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(Model model) {
        List<Map<String, Object>> list = lotteryService.list();
        model.addAttribute("list", list);
        return "/system/lottery/index";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public String save(Integer number) {
        ResultVo result = new ResultVo();
        try {
            List<Map<String, Object>> list = lotteryService.list();
            int size = list.size();
            Map<Integer, Map<String, Object>> map = new HashMap();
            Random random = new Random();
            while (map.size() < number) {
                int r = random.nextInt(size);
                map.put(r, list.get(r));
            }
            Set<Lottery> lotteries = new HashSet<>();
            list.clear();
            for (Map<String, Object> o : map.values()) {
                Lottery lottery = new Lottery();
                lottery.setCode((String) o.get("code"));
                lottery.setName((String) o.get("name"));
                lottery.setMobile((String) o.get("mobile"));
                lottery.setAddress((String) o.get("province") + o.get("city") + o.get("area") + o.get("detail"));
                lotteries.add(lottery);
                o.remove("code");
                list.add(o);
            }
            lotteryService.batchSave(lotteries);
            result.put("list", list);
            result.success();
        } catch (Exception e) {
            logger.error("保存中奖记录出错：{}", e);
        }
        return result.toJsonString();
    }

    public static void main(String[] args) {
        Random random = new Random();
        for (int i=0;i<100;i++) {
        System.out.println(random.nextInt(1611));
        }
    }
}
