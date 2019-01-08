package com.cb.service.order.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.exception.AppRollbackException;
import com.cb.common.exception.AppServiceException;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.*;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.dao.order.OrderDao;
import com.cb.model.active.Record;
import com.cb.model.common.AddressDatabase;
import com.cb.model.member.Member;
import com.cb.model.member.ScoreRecord;
import com.cb.model.order.Order;
import com.cb.model.order.OrderProduct;
import com.cb.model.product.Product;
import com.cb.model.security.User;
import com.cb.model.warehouse.Stock;
import com.cb.model.warehouse.Warehouse;
import com.cb.service.activition.RecordService;
import com.cb.service.common.AddressDatabaseService;
import com.cb.service.member.MemberService;
import com.cb.service.member.ScoreRecordService;
import com.cb.service.order.OrderProcessService;
import com.cb.service.order.OrderService;
import com.cb.service.payment.PaymentService;
import com.cb.service.product.ProductService;
import com.cb.service.security.UserService;
import com.cb.service.warehouse.StockService;
import com.cb.service.warehouse.WarehouseService;
import com.cb.vo.ResultVo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;

@Service
@Transactional
public class OrderServiceImpl extends CommonServiceImpl<Order, String> implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderProcessService orderProcessService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private ScoreRecordService scoreRecordService;

    @Autowired
    private AddressDatabaseService addressDatabaseService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private StockService stockService;

    @Autowired
    private PaymentService paymentService;

    @Override
    protected CommonDao<Order, String> getCommonDao() {
        return orderDao;
    }

    @Override
    public Order findByCode(String code) {
        return orderDao.findUnique(Restrictions.eq("code", code));
    }

    @Override
    public Map<String, Object> statistics(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = orderDao.statistics(params);
        for (Map<String, Object> map : list) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                List<Object> values = (List<Object>) result.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                }
                values.add(entry.getValue() == null ? 0 : entry.getValue());
                result.put(key, values);
            }
        }
        result.put("list", list);

        params.clear();
        params.put("todayStart", DateUtil.dateToString(DateUtil.newInstanceDateBegin(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("todayEnd", DateUtil.dateToString(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("weekStart", DateUtil.dateToString(DateUtil.firstDayOfWeek(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("weekEnd", DateUtil.dateToString(DateUtil.lastDayOfWeek(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("monthStart", DateUtil.dateToString(DateUtil.firstDayOfMonth(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("monthEnd", DateUtil.dateToString(DateUtil.lastDayOfMonth(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        List<Map<String, Object>> list2 = orderDao.statistics2(params);
        if (ListUtil.isNotEmpty(list2)) {
            List<Map.Entry<String, Object>> sortList = new ArrayList<>(list2.get(0).entrySet());
            Collections.sort(sortList, new Comparator<Map.Entry<String, Object>>() {
                //升序排序
                public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
            Map<String, Object> head = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : sortList) {
                head.put(entry.getKey().substring(1), entry.getValue() == null ? 0 : entry.getValue());
            }
            result.put("head", head);
        }
        return result;
    }

    @Override
    public Map<String, Object> rank(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = orderDao.rank(params);
        for (Map<String, Object> map : list) {
            for (String s : map.keySet()) {
                if (s.equals("level")) {
                    map.put(s, Member.LevelEnum.valueOf((String) map.get(s)).getDesc());
                }
            }
        }
        result.put("list", list);
        return result;
    }

    @Override
    public Map<String, Object> active(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = orderDao.active(params);
        for (Map<String, Object> map : list) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                List<Object> values = (List<Object>) result.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                }
                values.add(entry.getValue());
                result.put(key, values);
            }
        }
        List<Map<String, Object>> list2 = orderDao.active2(params);
        list.addAll(list2);
        result.put("list", list);

        params.clear();
        params.put("todayStart", DateUtil.dateToString(DateUtil.newInstanceDateBegin(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("todayEnd", DateUtil.dateToString(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("weekStart", DateUtil.dateToString(DateUtil.firstDayOfWeek(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("weekEnd", DateUtil.dateToString(DateUtil.lastDayOfWeek(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("monthStart", DateUtil.dateToString(DateUtil.firstDayOfMonth(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("monthEnd", DateUtil.dateToString(DateUtil.lastDayOfMonth(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        List<Map<String, Object>> list3 = orderDao.active3(params);
        if (ListUtil.isNotEmpty(list3)) {
            List<Map.Entry<String, Object>> sortList = new ArrayList<>(list3.get(0).entrySet());
            Collections.sort(sortList, new Comparator<Map.Entry<String, Object>>() {
                //升序排序
                public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
            Map<String, Object> head = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : sortList) {
                head.put(entry.getKey().substring(1), entry.getValue() == null ? 0 : entry.getValue());
            }
            result.put("head", head);
        }
        return result;
    }

    @Override
    public Page<Order> findPageByParams(Page<Order> page, Map<String, Object> params) {
        return orderDao.findPageByParams(page, params);
    }

    @Override
    public List<Map<String, Object>> findMapByParamsAlone(Map<String, Object> params) {
        return orderDao.findMapByParamsAlone(params);
    }

    @Override
    public Page<Map<String, Object>> findPageByMember(Page<Map<String, Object>> page, Map<String, Object> params) {
        return orderDao.findPageByMember(page, params);
    }

    @Override
    public Page<Order> findPageByMemberDetails(Page<Order> page, Map<String, Object> params) {
        return orderDao.findPageByMemberDetails(page, params);
    }

    @Override
    public Page<Map<String, Object>> findMapPageByParams(Page<Map<String, Object>> page, Map<String, Object> params) {
        return orderDao.findMapPageByParams(page, params);
    }

    @Override
    public Map<String, Object> findMapByMemberSum(Map<String, Object> params) {
        return orderDao.findMapByMemberSum(params);
    }

    @Override
    public Page<Map<String, Object>> getOrderList(String username, String password, Page<Map<String, Object>> page, Map<String, Object> params) {
        User user = userService.getUserByUsername(username);
        if (user != null && user.getType().equals(User.TypeEnum.OPEN)) {
            password = new SimpleHash(Constants.MD5, password, user.getSalt()).toString();
            if (password.equals(user.getPassword())) {
                params.put("type", Order.TypeEnum.ALONE);
                // TODO: 2017/1/24   测试时开放所有状态的订单查询，正式上线时加上限制
                params.put("status", Order.StatusEnum.DISTRIBUTION);
                page = orderDao.getOrderList(page, params);
                List<Map<String, Object>> list = page.getResult();
                if (ListUtil.isNotEmpty(list)) {
                    for (Map<String, Object> map : list) {
                        map.put("products", new ArrayList<Map<String, Object>>());
                        //处理产品信息
                        for (String key : map.keySet()) {
                            if (StringUtil.contains("productCode|productName|productQuantity|productAmount", key)) {
                                ArrayList<Map<String, Object>> products = (ArrayList<Map<String, Object>>) map.get("products");
                                String[] values = StringUtil.split((String) map.get(key), ",");
                                for (int i = 0; i < values.length; i++) {
                                    Map<String, Object> temp = new HashMap<>();
                                    if (products.size() > i) {
                                        temp = products.get(i);
                                        products.remove(temp);
                                    }
                                    temp.put(key, values[i]);
                                    products.add(i, temp);
                                }
                                map.put("products", products);
                            }
                        }
                        map.remove("productCode");
                        map.remove("productName");
                        map.remove("productQuantity");
                        map.remove("productAmount");
                    }
                }
            } else {
                throw new AppServiceException("没有权限");
            }
        } else {
            throw new AppServiceException("没有权限");
        }
        return page;
    }

    @Override
    public synchronized Order create(Order order) {
        Member member = memberService.findById(ShiroSecurityUtil.getCurrentMemberId());
        if (member == null) {
            throw new AppServiceException("未登录");
        }
        if (!member.getStatus().equals(Member.StatusEnum.SUCCESS)) {//通过了审核
            throw new AppServiceException("未认证，无法购买商品");
        }
        if (member.getLevel().equals(Member.LevelEnum.FAMILY)) {//亲情会员
            throw new AppServiceException("亲情会员无法购买商品");
        }
        String province = order.getProvince();
        if (StringUtil.isBlank(province)) {
            throw new AppServiceException("没有选择收货地址");
        }
        AddressDatabase address = addressDatabaseService.findById(province);
        if (address == null) {
            throw new AppServiceException("没有选择收货地址");
        }
        String warehouseCode = address.getDeliver();
        if (StringUtil.isBlank(warehouseCode)) {
            throw new AppServiceException("错误的发货地址");
        }
        Warehouse warehouse = order.getWarehouse();
        if (warehouse == null) {
            warehouse = warehouseService.findByCodeWithCache(warehouseCode);
        }
        if (warehouse == null) {
            throw new AppServiceException("错误的发货地址");
        }
        List<Warehouse> cacheWarehouses = warehouseService.findAll();
        List<Warehouse> warehouses = ListUtil.copyPropertiesInList(Warehouse.class, cacheWarehouses);
        warehouses.remove(warehouse);
        List<OrderProduct> orderProducts = order.getProducts();
        if (ListUtil.isEmpty(orderProducts)) {
            throw new AppServiceException("未选择商品");
        }
        BigDecimal amount = BigDecimal.ZERO;
        Integer number = 0;
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        Double weight = 0d;
        List<Product> products = new ArrayList<>();
        // TODO: 2017/3/24 活动暂时先这样写
        Record recordb = new Record();
        for (OrderProduct orderProduct : orderProducts) {

            recordb = setActivition(orderProduct, order);// TODO: 2017/3/24 活动

            List<Warehouse> unusewarehouses = new ArrayList<>();
            Integer quantity = orderProduct.getQuantity();
            Product product = productService.findByCode(orderProduct.getProduct().getCode());
            Stock stock = stockService.findByProductandWarehouse(product.getId(), warehouse.getId());
            if (product.getStatus().equals(Product.StatusEnum.OFF_THE_SHELVES)) {
                throw new AppServiceException("商品{0}已下架", product.getName());
            }
            //判断该产品是否可以销售
            ResultVo result = stockService.check(stock, quantity);
            if (!result.isSuccess()) {
                //throw new AppServiceException(result.getMessage());
                flag = true;
                products.add(product);
            }
            if (flag) {
                for (Warehouse w : warehouses) {
                    stock = stockService.findByProductandWarehouse(product.getId(), w.getId());
                    result = stockService.check(stock, quantity);
                    if (!result.isSuccess()) {
                        unusewarehouses.add(w);
                    }
                }
                warehouses.removeAll(unusewarehouses);
            } else {
                //订单购买产品信息
                BigDecimal unitPrice = product.getPriceByLevel(member.getLevel());  //获得对应会员用户的产品单价
                orderProduct.setUnit(product.getUnit());
                orderProduct.setUnitPrice(unitPrice);
                orderProduct.setAmount(unitPrice.multiply(new BigDecimal(quantity))); //计算单个产品总价
                orderProduct.setProduct(product);

                weight += product.getGrossWeight() * quantity; //计算产品重量
                amount = amount.add(orderProduct.getAmount());  //计算产品的金额
                number += quantity;

                //更新产品数据
                stockService.saveWithSale(stock, quantity);

                //保存宣传图
                sb.append(",");
                sb.append(product.getImage().getFilePath());
            }
        }
        if (flag) {
            Map<String, Object> map = new LinkedHashMap<>();
            List<Map<String, Object>> warehouseList = new ArrayList<>();
            List<String> productList = new ArrayList<>();
            map.put("size", warehouses.size() + "");
            for (Warehouse w : warehouses) {
                Map<String, Object> temp = new HashMap<>();
                temp.put("id", w.getId());
                temp.put("name", w.getName());
                warehouseList.add(temp);
            }
            for (Product product : products) {
                productList.add(product.getCode());
            }
            map.put("warehouses", warehouseList);
            map.put("products", productList);
            throw new AppRollbackException(JsonUtil.toFullJson(map));

        }
        order.setWarehouse(warehouse);
        order.setImagePaths(sb.toString().substring(1));

        //防止订单编号重复,订单编号为：年月日+4位随机数字
        String temp = DateUtil.dateToString(DateUtil.newInstanceDate(), "yyMMdd");
        order.setCode(temp + StringUtil.randomNumeric(4));
        while (orderDao.findUnique(Restrictions.eq("code", order.getCode())) != null) { //查询数据库是否存在
            logger.warn("订单编号重复：" + order.getCode());
            order.setCode(temp + StringUtil.randomNumeric(4));
        }

        //邮费计算
        BigDecimal postage;
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.POSTAGE_TIME), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) < 0) {
            postage = BigDecimal.ZERO;
            if (amount.compareTo(new BigDecimal(PropertiesUtil.getPropertiesValue(Constants.PROPERTY_FREE))) == -1) {
                postage = new BigDecimal(PropertiesUtil.getPropertiesValue(Constants.PROPERTY_POSTAGE));
            }
        } else {
            weight = 1.2 * weight;
            if (weight > 0) {
                if (weight <= 2) {
                    postage = new BigDecimal(6);
                } else {
                    postage = new BigDecimal(6).add(new BigDecimal(Math.ceil(weight - 2) * 2));
                }
                order.setWeight(weight);
            } else {
                postage = new BigDecimal(PropertiesUtil.getPropertiesValue(Constants.PROPERTY_POSTAGE));
            }
            /*//邮费减免
            if (amount.compareTo(new BigDecimal(PropertiesUtil.getPropertiesValue(Constants.PROPERTY_FREE))) != -1) {
                postage = BigDecimal.ZERO;
            }*/
        }


        // TODO: 2017/2/20 金卡会员可享首单免10元活动
        BigDecimal discount = BigDecimal.ZERO;
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) < 0
                && member.getFirst() && amount.compareTo(new BigDecimal(100)) >= 0) {
            discount = discount.add(new BigDecimal(10));
            member.setFirst(false);
        }

        //获得积分算法：付款金额每满百增加积分
        //增量视层级而定，订单所属用户增量5，上级增量6，上上级增量7
        BigDecimal payAmount = amount.add(postage).subtract(discount);
        Integer num = payAmount.divideToIntegralValue(new BigDecimal(100)).intValue();  //付款金额除百取整
        Integer score = num * 5;

        // TODO: 2017/3/2 下单积分抵扣活动,活动时间2017.3.8~2017.5.31
        Set<ScoreRecord> set = new HashSet<>();
        String name = member.getRealName();
        String mobile = member.getMobile();
        String code = order.getCode();
        Integer useScore = order.getUseScore();
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                && useScore > 0) {
            Integer dbScore = member.getAvailableScore();
            if (dbScore < useScore) {
                throw new AppServiceException("积分不足，抵扣失败！");
            }
            if (num * 10 < useScore) {
                throw new AppServiceException("订单金额不满足抵扣条件！");
            }
            member.setAvailableScore(dbScore - useScore);
            member.setScore(member.getScore() - useScore);
            discount = discount.add(new BigDecimal(useScore));
            set.add(new ScoreRecord("订货抵扣", -useScore, ScoreRecord.StatusEnum.ACTIVATION, MessageFormat.format("会员{0}({1})下单{2}订货使用女神券抵扣", name, mobile, code), member));
        }

        order.setAmount(amount);    //订单产品金额
        order.setPostage(postage);  //订单邮费
        order.setPayAmount(amount.add(postage).subtract(discount));  //订单应付金额
        //order.setScore(score);  //订单可获得积分
        order.setDiscount(discount);    //订单优惠金额
        order.setProductNum(number);    //订单商品数量
        order.setStatus(Order.StatusEnum.PAYMENTING);   //初始创建订单其状态为“待付款”
        order.setUser(member.getUser());    //订单所属用户
        order.setCreated(member.getUser()); //订单创建用户
        order.setType(Order.TypeEnum.ALONE);    //订单类型为“单例”

        save(order);
        if (StringUtil.isNotBlank(recordb.getMemberId())) {
            recordb.setCode(order.getCode());
            recordService.save(recordb);// TODO: 2017/3/24 完成订单后储存活动状态
//            WeixinUtil.sendWxParticipate(order);// TODO: 2017/3/24 模板消息推送，参与了活动
        }
        orderProcessService.save(order);

        // TODO: 2017/2/18 完成订单获得积分，暂时简单处理，后期写入活动
        if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) < 0) {
            member.setScore(member.getScore() + score);//当前会员加总积分
            member.setUnavailableScore(member.getUnavailableScore() + score);   //下单时增加积分放入用户冻结积分中，待订单完成时解冻
            set.add(new ScoreRecord("订货赠送", score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("会员{0}({1})下单{2}订货赠送", name, mobile, code), member));
            ShiroSecurityUtil.setAttribute(Constants.SESSION_CURRENT_MEMBER, member);   //在shiro缓存中保存会员信息
            memberService.save(member);

            member = member.getAgent();
            if (member != null) {
                score = num * 6;    //上级
                member.setScore(member.getScore() + score);
                member.setUnavailableScore(member.getUnavailableScore() + score);
                memberService.save(member);
                set.add(new ScoreRecord("下级订货赠送", score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("下级会员{0}({1})下单{2}订货赠送", name, mobile, code), member));
                member = member.getAgent();
                if (member != null) {
                    score = num * 7;    //上上级
                    member.setScore(member.getScore() + score);
                    member.setUnavailableScore(member.getUnavailableScore() + score);
                    memberService.save(member);
                    set.add(new ScoreRecord("间接下级订货赠送", score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("间接下级会员{0}({1})下单{2}订货赠送", name, mobile, code), member));
                }
            }
        } else {
            ShiroSecurityUtil.setAttribute(Constants.SESSION_CURRENT_MEMBER, member);   //在shiro缓存中保存会员信息
            memberService.save(member);
        }
        scoreRecordService.batchSave(set);
        return order;
    }

    @Override
    public void cancel(String code) {
        String userId = ShiroSecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new AppServiceException("未登录");
        }
        Order order = findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在");
        }
        if (order.getStatus().equals(Order.StatusEnum.CANCELED)) {
            throw new AppServiceException("订单已取消");
        }
        try {
            ShiroSecurityUtil.getSubject().checkRole("super");
            logger.warn("超级管理员取消订单:{}", order.getCode());
        } catch (Exception e) {
            if (!order.getUser().getId().equals(userId)) {
                throw new AppServiceException("没有取消权限");
            }
        }
        cancel(order, ShiroSecurityUtil.getCurrentUser(), DateUtil.newInstanceDate(), Order.StatusEnum.CANCELED);
        save(order);
    }

    @Override
    public void deliverSelf(String code) {
        String userId = ShiroSecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new AppServiceException("未登录");
        }
        Order order = findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在");
        }
        if (order.getStatus().equals(Order.StatusEnum.SELF)) {
            throw new AppServiceException("订单已代理发货");
        }
        if (!order.getUser().getId().equals(userId)) {
            throw new AppServiceException("没有发货权限");
        }
        deliver(order, ShiroSecurityUtil.getCurrentUser(), DateUtil.newInstanceDate());//自己发货
        save(order);
    }

    @Override
    public synchronized Order receive(String codes) {
        String memberId = ShiroSecurityUtil.getCurrentMemberId();
        if (memberId == null) {
            throw new AppServiceException("未登录");
        }
        Member member = memberService.findById(memberId);
        String[] temp = StringUtil.split(codes, ",");
        if (ArrayUtils.isEmpty(temp)) {
            throw new AppServiceException("参数不能为空");
        }
        List<Order> list = new ArrayList<>();
        Member agent = member.getAgent();
        Order order = new Order();
        BigDecimal income = BigDecimal.ZERO;    //收取金额
        BigDecimal amount = BigDecimal.ZERO;    //产品金额
        BigDecimal payAmount = BigDecimal.ZERO; //应付金额
        BigDecimal discount = BigDecimal.ZERO;   //优惠金额
        BigDecimal postage = BigDecimal.ZERO;   //邮费
        Integer score = 0;  //获得积分
        Integer useScore = 0;   //使用积分
        Integer num = 0;
        Double weight = 0d;
        List<String> imagePaths = new ArrayList<>();
        List<OrderProduct> products = new ArrayList<>();
        int sort = 0;
        //遍历所有确认付款订单
        for (String code : temp) {
            Order dbOrder = findByCode(code);
            if (dbOrder != null && dbOrder.getStatus().equals(Order.StatusEnum.PAYMENTING)) {
                //更改状态为审核中
                dbOrder.setStatus(Order.StatusEnum.AUDITING);
                save(dbOrder);
                list.add(dbOrder);

                income = income.add(dbOrder.getPayAmount());
                postage = postage.add(dbOrder.getPostage());
                num += dbOrder.getProductNum();
                weight += dbOrder.getWeight();
                useScore += dbOrder.getUseScore();
                discount = discount.add(dbOrder.getDiscount());
                if (agent == null) {
                    amount = amount.add(dbOrder.getAmount());
                    payAmount = payAmount.add(dbOrder.getPayAmount().subtract(dbOrder.getPostage()));   //此处减去邮费，后面会加上
                } else {
                    //订单可获得女神券
                    if (dbOrder.getType().equals(Order.TypeEnum.ALONE)) {
                        score += dbOrder.getPayAmount().divideToIntegralValue(new BigDecimal(100)).intValue() * 6;
                    } else {
                        for (Order child : dbOrder.getChildren()) {
                            if (child.getType().equals(Order.TypeEnum.ALONE)) {
                                score += child.getPayAmount().divideToIntegralValue(new BigDecimal(100)).intValue() * 7;
                            }
                        }
                    }
                }

                //保存产品
                for (OrderProduct dbProduct : dbOrder.getProducts()) {
                    OrderProduct product = new OrderProduct();
                    BeanUtil.copyPropertiesWithoutNullValues(product, dbProduct);   //拷贝子订单的产品信息
                    product.setId(null);
                    product.setOrder(order);
                    products.add(product);

                    String img = dbProduct.getProduct().getImage().getFilePath();
                    if (imagePaths.indexOf(img) == -1) {
                        imagePaths.add(img);
                    }

                    if (agent != null) {
                        //根据代理等级获得相应的产品价格
                        BigDecimal unitPrice = product.getProduct().getPriceByLevel(agent.getLevel());
                        product.setUnitPrice(unitPrice);
                        product.setAmount(unitPrice.multiply(new BigDecimal(product.getQuantity())));
                        amount = amount.add(product.getAmount());
                        payAmount = payAmount.add(product.getAmount());
                    }
                }
            }
        }
        if (list.size() == 0) {
            throw new AppServiceException("没有可操作的订单");
        }
        String paths = imagePaths.toString();
        order.setImagePaths(paths.substring(1, paths.length() - 1));
        order.setProductNum(num);
        order.setProducts(products);
        order.setPay(Order.PayEnum.OFFLINE);
        order.setIncome(income);
        order.setAmount(amount);
        order.setPostage(postage);
        order.setDiscount(discount);
        order.setUseScore(useScore);
        order.setWeight(weight);
        if (agent != null) {
            order.setStatus(Order.StatusEnum.RECEIVEING);
            order.setUser(agent.getUser());
            order.setType(Order.TypeEnum.MERGE);
            order.setScore(score);
            order.setPayAmount(payAmount.add(postage).subtract(discount));
        } else {
            order.setStatus(Order.StatusEnum.PAYMENTING);
            order.setPay(Order.PayEnum.WECHAT);

            order.setUser(member.getAuditUser());//没有上级代理，更改为系统订单
            order.setType(Order.TypeEnum.SYSTEM);
            order.setPayAmount(payAmount.add(postage));
        }
        order.setCreated(member.getUser());

        //订单编号
        String tempStr = DateUtil.dateToString(DateUtil.newInstanceDate(), "yyMMdd");
        String code = tempStr + StringUtil.randomNumeric(4);
        while (orderDao.findUnique(Restrictions.eq("code", code)) != null) {
            logger.warn("订单编号重复：" + code);
            code = tempStr + StringUtil.randomNumeric(4);
        }
        order.setCode(code);
        //确认付款 钻石不用提醒上级审核.
        if (!member.getLevel().equals(Member.LevelEnum.DIAMOND)) {
            WeixinUtil.sendReviewOrderMessage(order);
        }

        for (Order o : list) {
            o.setParent(order);
        }
        order.setChildren(list);
        save(order);
        if (agent != null) {
            orderProcessService.save(order);
        }
        return order;
    }

    @Override
    public void auditByMember(String code) {
        User user = ShiroSecurityUtil.getCurrentUser();
        if (user == null) {
            throw new AppServiceException("未登录");
        }
        Order order = findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在 ");
        }
        if (order.getStatus().ordinal() > Order.StatusEnum.AUDITING.ordinal()) {
            throw new AppServiceException("订单{0}", order.getStatus().getDesc());
        }
        //如果不是待收款
        if (!order.getStatus().equals(Order.StatusEnum.RECEIVEING)) {
            throw new AppServiceException("订单{0}", order.getStatus().getDesc());
        }
        order.setStatus(Order.StatusEnum.PAYMENTING);
        for (Order child : order.getChildren()) {
            WeixinUtil.sendWxConfirmOrder(child, "wx");//微信代理确认收款
        }
        save(order);
        orderProcessService.save(order);
    }

    @Override
    public void auditByService(String code, String[] codes, String[] warehouses, BigDecimal amount, String remark, String account, String source) {
        User user = ShiroSecurityUtil.getCurrentUser();
        if (user == null) {
            throw new AppServiceException("未登录");
        }
        Order order = findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在 ");
        }
        if (!Order.StatusEnum.FINREVOKED.equals(order.getStatus()) && order.getStatus().ordinal() > Order.StatusEnum.AUDITING.ordinal()) {
            throw new AppServiceException("订单{0}", order.getStatus().getDesc());
        }
        //客服审核 客服回退，代付款的系统单
        if (order.getStatus().equals(Order.StatusEnum.RECEIVEING) || order.getStatus().equals(Order.StatusEnum.FINREVOKED)) {
            ShiroSecurityUtil.getSubject().checkPermission("audit");    //判断是否有“订单审核”权限
            Date date = DateUtil.newInstanceDate();
            order.setStatus(Order.StatusEnum.AUDITING);
            order.setAuditDate(date);
            order.setAuditUser(user);
            order.setPayAmount(amount); //实收金额
            order.setRemark(remark);    //备注信息
            order.setAccount(account);//账号
            order.setSource(source);//来源
            if (codes != null && warehouses != null && codes.length > 0 && warehouses.length > 0 && codes.length == warehouses.length) {
                Set<Order> set = new HashSet<>();//配货仓库处理
                for (int i = 0; i < codes.length; i++) {
                    Order db = findByCode(codes[i]);
                    if (db != null) {
                        db.setStatus(Order.StatusEnum.AUDITING);
                        db.setAuditDate(date);
                        if (StringUtil.isNotBlank(warehouses[i])) {
                            db.setWarehouse(warehouseService.findByCodeWithCache(warehouses[i]));
                        } else {
                            db.setWarehouse(null);
                        }
                        set.add(db);
                    }
                }
                batchSave(set);
            }
        } else {
            throw new AppServiceException("订单" + order.getStatus().getDesc());
        }
        save(order);
    }

    @Override
    public void auditByFinance(String code) {
        User user = ShiroSecurityUtil.getCurrentUser();
        if (user == null) {
            throw new AppServiceException("未登录");
        }
        Order order = findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在 ");
        }
        if (order.getStatus().ordinal() > Order.StatusEnum.AUDITING.ordinal()) {
            throw new AppServiceException("订单{0}", order.getStatus().getDesc());
        }
        if (order.getStatus().equals(Order.StatusEnum.AUDITING)) {//财务审核
            ShiroSecurityUtil.getSubject().checkPermission("finance");  //判断是否有“财务审核”权限
            change(order, user, DateUtil.newInstanceDate());
        } else {
            throw new AppServiceException("订单" + order.getStatus().getDesc());
        }
        save(order);
    }

    @Override
    public void auditByAuto() {
        List<Order> list = orderDao.findByAutoAudit();
        User user = new User();
        user.setId("system");//表示为系统自动审核
        for (Order order : list) {
            if (order == null) {
                throw new AppServiceException("订单不存在 ");
            }
            if (!order.getStatus().equals(Order.StatusEnum.RECEIVEING)) {
                throw new AppServiceException("订单{0}", order.getStatus().getDesc());
            }
            try {
                changeByAuto(order, user, DateUtil.newInstanceDate());
                save(order);
            } catch (Exception e) {
                logger.error("系统自动审核订单出错：{}", e);
            }
        }
    }

    @Override
    public void deliver(String username, String password, String code, String express, String expressNumber) {
        User user = userService.getUserByUsername(username);
        if (user != null && user.getType().equals(User.TypeEnum.OPEN)) {
            password = new SimpleHash(Constants.MD5, password, user.getSalt()).toString();
            if (password.equals(user.getPassword())) {
                Order order = findByCode(code);
                // TODO: 2017/1/24  测试时开放所有状态的订单可执行发货，正式上线时加上限制
                if (order != null && order.getStatus().equals(Order.StatusEnum.DISTRIBUTION)) {
                    order.setStatus(Order.StatusEnum.TRANSPORTATION);
                    order.setExpress(express);
                    order.setExpressNumber(expressNumber);
                    order.setDeliverDate(DateUtil.newInstanceDate());
                    order.setDeliverUser(user);
                    for (OrderProduct orderProduct : order.getProducts()) {
                        Stock stock = stockService.findByProductandWarehouse(orderProduct.getProduct().getId(), order.getWarehouse().getId());
                        stockService.saveWithDeliver(stock, orderProduct.getQuantity());
                    }
                    save(order);
                    orderProcessService.save(order);
                } else {
                    throw new AppServiceException("订单不存在");
                }
            } else {
                throw new AppServiceException("没有权限");
            }
        } else {
            throw new AppServiceException("没有权限");
        }
    }


    @Override
    public void receipt(String code) {
        User user = ShiroSecurityUtil.getCurrentUser();
        if (user == null) {
            throw new AppServiceException("未登录");
        }
        Order order = findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在");
        }
        if (!order.getStatus().equals(Order.StatusEnum.TRANSPORTATION)) {
            throw new AppServiceException("不能确认收货");
        }
        if (!StringUtil.equals(order.getUser().getId(), user.getId())) {
            throw new AppServiceException("无确认收货权限");
        }
        order.setStatus(Order.StatusEnum.COMPLETED);
        save(order);
        orderProcessService.save(order);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void revoke(String code, String remark) {
        User user = ShiroSecurityUtil.getCurrentUser();
        Order order = findByCode(code);
        //客服取消
        if (order.getStatus().equals(Order.StatusEnum.RECEIVEING) || order.getStatus().equals(Order.StatusEnum.FINREVOKED) || order.getStatus().equals(Order.StatusEnum.COMPLETED)) {//包含被财务退回的
            ShiroSecurityUtil.getSubject().checkPermission("audit");    //判断是否有“订单审核”权限
            cancel(order, ShiroSecurityUtil.getCurrentUser(), DateUtil.newInstanceDate(), Order.StatusEnum.CUSCANCELED);
        } else {
            throw new AppServiceException("订单" + order.getStatus().getDesc());
        }
        order.setAuditDate(DateUtil.newInstanceDate());
//        order.setFinanceDate(DateUtil.newInstanceDate());//取消的系统单是否需要设置财务审核时间
        order.setAuditUser(user);
        paymentService.refund(order);
        orderDao.save(order);
    }

    @Override
    public void revokeOrder(String id, String remark) {
        //财务回退
        User user = ShiroSecurityUtil.getCurrentUser();
        Order order = findById(id);
        //只有审核中的单可回退
        if (order.getStatus().equals(Order.StatusEnum.AUDITING)) {
            order.setStatus(Order.StatusEnum.FINREVOKED);
            orderProcessService.save(order);
        } else {
            throw new AppServiceException("订单" + order.getStatus().getDesc());
        }
        order.setAuditDate(DateUtil.newInstanceDate());
        order.setAuditUser(user);
        save(order);
    }

    @Override
    public void delete(String code) {
        String userId = ShiroSecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new AppServiceException("未登录");
        }
        Order order = findByCode(code);
        if (order == null) {
            throw new AppServiceException("订单不存在");
        }
        if (!userId.equals(order.getUser().getId())) {
            throw new AppServiceException("没有权限");
        }
        order.setDeleted(true);
        save(order);
    }

    @Override
    public Integer customer(Date date) {
        return orderDao.customer(date);
    }

    private void cancel(Order order, User user, Date date, Order.StatusEnum systemStatus) {
        if (order.getType().equals(Order.TypeEnum.SYSTEM) && Order.StatusEnum.CUSCANCELED.equals(systemStatus)) {//客服取消
            order.setStatus(Order.StatusEnum.CUSCANCELED);
        } else if (order.getType().equals(Order.TypeEnum.SYSTEM) && Order.StatusEnum.FINCANCELED.equals(systemStatus)) {//财务取消
            order.setStatus(Order.StatusEnum.FINCANCELED);
        } else {//用户取消
            order.setStatus(Order.StatusEnum.CANCELED);
        }
        order.setAuditUser(user);
        order.setAuditDate(date);
        // TODO: 2017/3/24 活动暂时先这样写
        Record record = new Record();
        if (order.getType().equals(Order.TypeEnum.ALONE)) {
            //取消订单恢复产品数据
            for (OrderProduct orderProduct : order.getProducts()) {

                record = cancelActivition(orderProduct, order);//恢复活动数据与资格

//                productService.saveProductWithCancel(orderProduct.getProduct(), orderProduct.getQuantity());
                Stock stock = stockService.findByProductandWarehouse(orderProduct.getProduct().getId(), order.getWarehouse().getId());
                stockService.saveWithCancel(stock, orderProduct.getQuantity());
            }

            Set<ScoreRecord> set = new HashSet<>();
            Member member = order.getUser().getMember();
            String name = member.getRealName();
            String mobile = member.getMobile();
            String code = order.getCode();

            // TODO: 2017/3/3 取消订单返还抵扣的积分
            Integer useScore = order.getUseScore();
            if (DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                    DateUtil.newInstanceDate().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                    && useScore > 0) {
                member.setAvailableScore(member.getAvailableScore() + useScore);
                member.setScore(member.getScore() + useScore);
                set.add(new ScoreRecord("取消订单返还", useScore, ScoreRecord.StatusEnum.ACTIVATION, MessageFormat.format("会员{0}({1})取消下单{2}订货，返还其所抵扣女神券", name, mobile, code), member));
                memberService.save(member);
            }

            // TODO: 2017/2/18 取消订单减少积分，暂时简单处理，后期写入活动
            if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) < 0) {
                Integer num = order.getPayAmount().divideToIntegralValue(new BigDecimal(100)).intValue();
                Integer score = num * 5;
                member.setScore(member.getScore() - score);
                member.setUnavailableScore(member.getUnavailableScore() - score);
                memberService.save(member);
                set.add(new ScoreRecord("取消订单扣除", -score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("会员{0}({1})取消订单{2}扣除", name, mobile, code), member));
                member = member.getAgent();
                if (member != null) {
                    score = num * 6;
                    member.setScore(member.getScore() - score);
                    member.setUnavailableScore(member.getUnavailableScore() - score);
                    memberService.save(member);
                    set.add(new ScoreRecord("下级会员取消订单扣除", -score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("下级会员{0}({1})取消订单{2}扣除", name, mobile, code), member));
                    member = member.getAgent();
                    if (member != null) {
                        score = num * 7;
                        member.setScore(member.getScore() - score);
                        member.setUnavailableScore(member.getUnavailableScore() - score);
                        memberService.save(member);
                        set.add(new ScoreRecord("间接下级取消订单扣除", -score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("间接下级会员{0}({1})取消订单{2}扣除", name, mobile, code), member));
                    }
                }
            }
            scoreRecordService.batchSave(set);
        }
        orderProcessService.save(order);
        if (StringUtil.isNotBlank(record.getMemberId())) {
            recordService.save(record); // TODO: 2017/3/24 完成订单操作后再设置为
        }
        //被驳回 上级驳回,客服驳回
        if (!order.getUser().getId().equals(user.getId()) && user.getType().equals(User.TypeEnum.WECHAT)) {
            WeixinUtil.sendWxCancelOrder(order, user, PropertiesUtil.getPropertiesValue(Constants.CANCEL_ORDER_TEMPLATE_FIRST2));
        }
        //这里遍历子单
        for (Order child : order.getChildren()) {
            cancel(child, user, date, Order.StatusEnum.CANCELED);
        }
    }

    private void deliver(Order order, User user, Date date) {
        order.setStatus(Order.StatusEnum.SELF);
        order.setAuditUser(user);
        order.setAuditDate(date);
        if (!order.getUser().getId().equals(user.getId())) {
            WeixinUtil.sendWxCancelOrder(order, user, PropertiesUtil.getPropertiesValue(Constants.CANCEL_ORDER_TEMPLATE_FIRST1));//自己发货
        }
        // TODO: 2017/3/24 活动暂时先这样写
        Record record = new Record();
        for (OrderProduct orderProduct : order.getProducts()) {
            record = cancelActivition(orderProduct, order);
        }
        if (order.getType().equals(Order.TypeEnum.ALONE)) {
            //代理发货订单恢复产品数据
            for (OrderProduct orderProduct : order.getProducts()) {
//                productService.saveProductWithCancel(orderProduct.getProduct(), orderProduct.getQuantity());
                Stock stock = stockService.findByProductandWarehouse(orderProduct.getProduct().getId(), order.getWarehouse().getId());
                stockService.saveWithCancel(stock, orderProduct.getQuantity());
            }
        }
        if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) < 0 &&
                order.getType().equals(Order.TypeEnum.ALONE)) {
            Set<ScoreRecord> set = new HashSet<>();
            Member member = order.getUser().getMember();
            String name = member.getRealName();
            String mobile = member.getMobile();
            String code = order.getCode();
            String parentName = user.getMember().getRealName();
            String parentMobile = user.getMember().getMobile();
            Integer num = order.getPayAmount().divideToIntegralValue(new BigDecimal(100)).intValue();
            Integer score = num * 5;
            member.setScore(member.getScore() - score);
            member.setUnavailableScore(member.getUnavailableScore() - score);
            memberService.save(member);
            if (user.getId().equals(member.getAgent().getUser().getId())) {
                set.add(new ScoreRecord("上级会员代理发货", -score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("会员{0}({1})订单{2}，由代理{3}({4})自己发货，扣除积分", name, mobile, code, parentName, parentMobile), member));
            } else {
                set.add(new ScoreRecord("间接上级会员代理发货", -score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("会员{0}({1})订单{2}，由代理{3}({4})自己发货，扣除积分", name, mobile, code, parentName, parentMobile), member));
            }
            member = member.getAgent();
            if (member != null) {
                score = num * 6;
                member.setScore(member.getScore() - score);
                member.setUnavailableScore(member.getUnavailableScore() - score);
                memberService.save(member);
                set.add(new ScoreRecord("上级会员代理发货", -score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("会员{0}({1})订单{2}，由代理{3}({4})自己发货，扣除积分", name, mobile, code, parentName, parentMobile), member));
                member = member.getAgent();
                if (member != null) {
                    score = num * 7;
                    member.setScore(member.getScore() - score);
                    member.setUnavailableScore(member.getUnavailableScore() - score);
                    memberService.save(member);
                    set.add(new ScoreRecord("会员代理发货", -score, ScoreRecord.StatusEnum.FROZEN, MessageFormat.format("会员{0}({1})订单{2}，由代理{3}({4})自己发货，扣除积分", name, mobile, code, parentName, parentMobile), member));
                }
            }
            scoreRecordService.batchSave(set);
        }
        orderProcessService.save(order);
        if (StringUtil.isNotBlank(record.getMemberId())) {
            recordService.save(record);// TODO: 2017/3/24 活动写死了
        }
        for (Order child : order.getChildren()) {
            deliver(child, user, date);
        }
    }

    private void change(Order order, User user, Date date) {
        logger.info("财务审核" + order.getCode());
        order.setFinanceUser(user);
        order.setFinanceDate(date);
        if (ListUtil.isNotEmpty(order.getChildren())) {
            //更改为已完成 并单不需要收货,财务审核通过就是完成
//            WeixinUtil.sendWxConfirmOrder(order);
//            if (order.getType().equals(Order.TypeEnum.MERGE)) {
            order.setStatus(Order.StatusEnum.COMPLETED);
//            } else {
//                order.setStatus(Order.StatusEnum.DISTRIBUTION);
//            }
            for (Order child : order.getChildren()) {
                change(child, user, date);
            }
        } else {//子单
            //更改为配货中 底层单需要收货,财务审核后开始配货
            order.setStatus(Order.StatusEnum.DISTRIBUTION);
            // TODO: 2017/2/18 财务审核完订单后激活女神券，暂时简单处理，后期写入活动
            if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) < 0) {
                Set<ScoreRecord> set = new HashSet<>();
                Member member = order.getUser().getMember();
                String name = member.getRealName();
                String mobile = member.getMobile();
                String code = order.getCode();
                Integer num = order.getPayAmount().divideToIntegralValue(new BigDecimal(100)).intValue();
                Integer score = num * 5;
                member.setAvailableScore(member.getAvailableScore() + score);
                member.setUnavailableScore(member.getUnavailableScore() - score);
                memberService.save(member);
                set.add(new ScoreRecord("订货赠送", score, ScoreRecord.StatusEnum.ACTIVATION, MessageFormat.format("会员{0}({1})订单{2}订货通过财务审核赠送", name, mobile, code), member));
                member = member.getAgent();
                if (member != null) {
                    score = num * 6;
                    member.setAvailableScore(member.getAvailableScore() + score);
                    member.setUnavailableScore(member.getUnavailableScore() - score);
                    memberService.save(member);
                    set.add(new ScoreRecord("下级订货赠送", score, ScoreRecord.StatusEnum.ACTIVATION, MessageFormat.format("下级会员{0}({1})订单{2}订货通过财务审核赠送", name, mobile, code), member));
                    member = member.getAgent();
                    if (member != null) {
                        score = num * 7;
                        member.setAvailableScore(member.getAvailableScore() + score);
                        member.setUnavailableScore(member.getUnavailableScore() - score);
                        memberService.save(member);
                        set.add(new ScoreRecord("间接下级订货赠送", score, ScoreRecord.StatusEnum.ACTIVATION, MessageFormat.format("间接下级会员{0}({1})订单{2}订货通过财务审核赠送", name, mobile, code), member));
                    }
                }
                scoreRecordService.batchSave(set);
            }
        }
        if (!"SYSTEM".equals(order.getType().name())) {
            WeixinUtil.sendWxConfirmOrder(order, "finance");
        }
        orderProcessService.save(order);//自动更改订单进程
    }

    private void changeByAuto(Order order, User user, Date date) {
        logger.info("系统自动审核" + order.getCode());
        order.setFinanceUser(user);
        order.setFinanceDate(date);
        if (ListUtil.isNotEmpty(order.getChildren())) {
            order.setStatus(Order.StatusEnum.COMPLETED);
            for (Order child : order.getChildren()) {
                changeByAuto(child, user, date);
            }
        } else {//子单
            //更改为配货中 底层单需要收货,财务审核后开始配货
            order.setStatus(Order.StatusEnum.DISTRIBUTION);

            // TODO: 2017/4/19 啊啊啊啊啊啊啊啊
            boolean special = false;
            for (OrderProduct orderProduct : order.getProducts()) {
                if (orderProduct.getProduct().getCode().equals("100230")) {
                    special = true;
                    break;
                }
            }
            // TODO: 2017/2/18 财务审核完订单后激活女神券，暂时简单处理，后期写入活动
            if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.USE_SCORE_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) < 0) {
                Set<ScoreRecord> set = new HashSet<>();
                Member member = order.getUser().getMember();
                String name = member.getRealName();
                String mobile = member.getMobile();
                String code = order.getCode();
                Integer num = order.getPayAmount().divideToIntegralValue(new BigDecimal(100)).intValue();
                Integer score = num * 5;
                member.setAvailableScore(member.getAvailableScore() + score);
                member.setUnavailableScore(member.getUnavailableScore() - score);
                memberService.save(member);
                set.add(new ScoreRecord("订货赠送", score, ScoreRecord.StatusEnum.ACTIVATION, MessageFormat.format("会员{0}({1})订单{2}订货通过系统自动审核赠送", name, mobile, code), member));
                member = member.getAgent();
                if (member != null) {
                    score = num * 6;
                    member.setAvailableScore(member.getAvailableScore() + score);
                    member.setUnavailableScore(member.getUnavailableScore() - score);
                    memberService.save(member);
                    set.add(new ScoreRecord("下级订货赠送", score, ScoreRecord.StatusEnum.ACTIVATION, MessageFormat.format("下级会员{0}({1})订单{2}订货通过系统自动审核赠送", name, mobile, code), member));
                    member = member.getAgent();
                    if (member != null) {
                        score = num * 7;
                        member.setAvailableScore(member.getAvailableScore() + score);
                        member.setUnavailableScore(member.getUnavailableScore() - score);
                        memberService.save(member);
                        set.add(new ScoreRecord("间接下级订货赠送", score, ScoreRecord.StatusEnum.ACTIVATION, MessageFormat.format("间接下级会员{0}({1})订单{2}订货通过系统自动审核赠送", name, mobile, code), member));
                    }
                }
                scoreRecordService.batchSave(set);
            }
        }
        orderProcessService.save(order);//自动更改订单进程
        //口红一期活动期间下单的订单 // TODO: 2017/4/12 口红一期活动审核微信通知
        if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                && Order.TypeEnum.ALONE.equals(order.getType())) {//是子单
            for (OrderProduct orderProduct : order.getProducts()) {//是否包含活动商品
                if (PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE).equals(orderProduct.getProduct().getCode())) {//包含限购活动商品
                    WeixinUtil.sendWxParticipate(order);
                }
            }
        }
    }

    private Record setActivition(OrderProduct orderProduct, Order order) {
        Record recordb = new Record();
        // TODO: 2017/3/23 注意活动时间 这个是一号购买
        if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                ) {//下单时间
            if (PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE).equals(orderProduct.getProduct().getCode())) {//包含限购活动商品
                if (orderProduct.getQuantity() > 1) {
                    throw new AppServiceException("您选购商品超过了限额");
                }
                Record recordCurrent = recordService.isExist(ShiroSecurityUtil.getCurrentMemberId());//下单设置为当前会员
                if (recordCurrent != null) {//有记录
                    if (Record.StatusEnum.YES.equals(recordCurrent.getStatus())) {//状态是已参与
                        //不可参与活动，可能已参与过了
                        throw new AppServiceException("您已参与过了活动");
                    } else {
                        //可以参加活动
                        recordCurrent.setStatus(Record.StatusEnum.YES);
                        recordb = recordCurrent;
                    }
                } else {//没有记录
                    //成功参与活动，保存记录
                    recordb.setMemberId(ShiroSecurityUtil.getCurrentMemberId());//下单设置为当前会员
                    recordb.setStatus(Record.StatusEnum.YES);//设置未参与了活动，完成该订单时更改未参与。取消订单的话可以再次参与
                }
            }
        }

        // TODO: 2017/3/23 注意活动时间 这个是八号购买
        if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_START_B), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END_B), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                ) {//下单时间
            if (PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE).equals(orderProduct.getProduct().getCode())) {//包含限购活动商品
                Record record = recordService.isExist(ShiroSecurityUtil.getCurrentMemberId());//下单设置为当前会员
                if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {//存在记录并且参与了活动
                    if (orderProduct.getQuantity() > 2) {//选择的商品超过的限额
                        throw new AppServiceException("该商品一次最多只能购买两盒");
                    } else if (orderProduct.getQuantity() > record.getNumber()) {//选择的商品超过的限额
                        throw new AppServiceException("您选购商品超过了限额");
                    } else {
                        int number1 = record.getNumber() - orderProduct.getQuantity();
                        if (number1 >= 0) {
                            record.setNumber(number1);
                        } else {//没有了数额
                            record.setNumber(0);
                        }
                    }
                } else {
                    throw new AppServiceException("很抱歉，您没有抢购资格");
                }
            }
        }
        return recordb;
    }

    private Record cancelActivition(OrderProduct orderProduct, Order order) {
        Record recordb = new Record();
        // TODO: 2017/3/23 注意活动时间 一号
        if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_START), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                && order.getType().equals(Order.TypeEnum.ALONE)) {
            if (PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE).equals(orderProduct.getProduct().getCode())) {//包含限购活动商品
                Record recordCurrent = recordService.isExist(order.getUser().getMember().getId());
                if (recordCurrent != null) {//有记录
                    if (Record.StatusEnum.YES.equals(recordCurrent.getStatus())) {//状态是已参与
                        recordCurrent.setStatus(Record.StatusEnum.CANNOT);//取消订单更改为未参与
                        recordb = recordCurrent;
                    }
                }
            }
        }
        // TODO: 2017/3/23 注意活动时间 八号
        if (order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_START_B), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) >= 0 &&
                order.getCreationTime().compareTo(DateUtil.stringToDate(PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_END_B), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS)) <= 0
                && order.getType().equals(Order.TypeEnum.ALONE)) {
            if (PropertiesUtil.getPropertiesValue(Constants.ACTIVITY_LIPSTICK_PRODUCT_CODE).equals(orderProduct.getProduct().getCode())) {//包含限购活动商品
                Record record = recordService.isExist(order.getUser().getMember().getId());
                if (record != null && Record.StatusEnum.YES.equals(record.getStatus())) {//存在记录并且参与了活动
                    int number1 = record.getNumber() + orderProduct.getQuantity();
                    record.setNumber(number1);//恢复数据
                }
            }
        }
        return recordb;
    }


}
