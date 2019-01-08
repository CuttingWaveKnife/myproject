package com.cb.model.order;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import com.cb.model.payment.PaymentRecord;
import com.cb.model.security.User;
import com.cb.model.warehouse.Warehouse;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "o_order")
@Where(clause = Constants.DELETED_FALSE)
public class Order extends CommonEntity {

    public enum StatusEnum {
        RECEIVEING("待收款", "客服审核中", "", "1,10", "1"),
        PAYMENTING("待付款", "会员待支付", "1,13", "2,11", "0"),
        AUDITING("审核中", "财务审核中", "2,3,4,5,6,14", "3,4,5,12", "2"),
        DISTRIBUTION("配货中", "", "7", "", ""),
        TRANSPORTATION("运输中", "", "8", "", ""),
        COMPLETED("已完成", "财务审核完成", "9", "6", "3"),
        SELF("代理发货", "", "10,11", "7,8", ""),
        CANCELED("已取消", "会员取消支付", "12", "9", "7"),
        FINREVOKED("财务已回撤", "财务已回撤", "14", "11", "4"),
        CUSCANCELED("客服已取消", "客服已取消", "13", "10", "6"),
        FINCANCELED("财务已取消", "财务已取消", "15", "12", "5");

        private String desc;

        private String sysDesc;

        private String processAlone;

        private String processMerge;

        private String processSystem;

        private StatusEnum(String desc, String sysDesc, String processAlone, String processMerge, String processSystem) {
            this.desc = desc;
            this.sysDesc = sysDesc;
            this.processAlone = processAlone;
            this.processMerge = processMerge;
            this.processSystem = processSystem;
        }

        public String getDesc() {
            return desc;
        }

        public String getSysDesc() {
            return sysDesc;
        }

        public String getProcessAlone() {
            return processAlone;
        }

        public String getProcessMerge() {
            return processMerge;
        }

        public String getProcessSystem() {
            return processSystem;
        }
    }

    public enum TypeEnum {
        //通过下单提交的订单
        ALONE("单独订单"),
        //通过付款提交的订单
        MERGE("合并订单"),
        //提交到后台审核的订单
        SYSTEM("系统订单");

        private String desc;

        private TypeEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum PayEnum {
        OFFLINE("线下支付"), ALIPAY("支付宝"), WECHAT("微信支付");

        private String desc;

        private PayEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 京东仓库枚举
     */
    public enum WarehouseEnum {

        GUANGZHOU("110005784", "广州公共平台4号库"),
        BEIJING("110007478", "北京公共平台2号库"),
        SHANGHAI("110007402", "上海百货服装仓A2库（新）"),
        WUHAN("110002870", "武汉公共平台1号库");

        private String code;

        private String desc;

        private WarehouseEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    private String code;    //编号

    private Integer productNum; //商品数量

    private StatusEnum status;  //状态

    private TypeEnum type;  //类型

    private PayEnum pay;    //支付方式

    private PaymentRecord paymentRecord;    //支付记录

    private BigDecimal income = BigDecimal.ZERO;  //应收金额

    private BigDecimal amount = BigDecimal.ZERO;  //商品总金额

    private BigDecimal postage = BigDecimal.ZERO; //邮费

    private BigDecimal discount = BigDecimal.ZERO;    //优惠费用

    private BigDecimal payAmount = BigDecimal.ZERO;   //实际支付费用

    private Integer score = 0;  //获得积分

    private Integer useScore = 0;   //使用积分

    private Double weight;  //产品重量

    private String remark;  //备注

    private String source;  //会员付款来源

    private String account;  //会员付款帐号

    private String name;    //收货人

    private String mobile;  //收货人电话

    private String province;    //省

    private String provinceName;    //省份中文名

    private String city;    //市

    private String cityName;    //城市中文名

    private String area;    //区

    private String areaName;    //区域中文名

    private String detail;  //详细地址

    private String full;    //地址全称

    private String express; //快递公司

    private String expressNumber;   //快递单号

    private String imagePaths;  //产品图片地址集

    private String process; //最新流程描述

    private WarehouseEnum deliverAddress;  //京东发货仓库地址

    private Date auditDate; //审核时间

    private Date financeDate; //财务审核时间

    private Date deliverDate; //发货时间

    private Warehouse warehouse;   //发货仓库

    private User auditUser; //审核人

    private User financeUser; //财务审核人

    private User deliverUser; //发货人

    private User user;  //订单所属用户

    private User created;   //订单创建用户

    private Order parent;   //父级订单

    private List<Order> children;   //子订单集合

    private List<OrderProduct> products;    //订单产品集合

    private List<OrderProcess> processes;   //订单流程

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "product_num")
    public Integer getProductNum() {
        return productNum;
    }

    public void setProductNum(Integer productNum) {
        this.productNum = productNum;
    }

    @Enumerated(value = EnumType.STRING)
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    @Enumerated(value = EnumType.STRING)
    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    @Enumerated(value = EnumType.STRING)
    public PayEnum getPay() {
        return pay;
    }

    public void setPay(PayEnum pay) {
        this.pay = pay;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", unique = true)
    public PaymentRecord getPaymentRecord() {
        return paymentRecord;
    }

    public void setPaymentRecord(PaymentRecord paymentRecord) {
        this.paymentRecord = paymentRecord;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Column(name = "use_score")
    public Integer getUseScore() {
        return useScore;
    }

    public void setUseScore(Integer useScore) {
        this.useScore = useScore;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public BigDecimal getPostage() {
        return postage;
    }

    public void setPostage(BigDecimal postage) {
        this.postage = postage;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    @Column(name = "pay_amount")
    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    @Column(name = "province_name")
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Column(name = "city_name")
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Column(name = "area_name")
    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public String getExpress() {
        return express;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    @Column(name = "express_number")
    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber;
    }

    @Column(name = "image_paths")
    public String getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(String imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Column(name = "deliver_address")
    @Enumerated(EnumType.STRING)
    public WarehouseEnum getDeliverAddress() {
        return deliverAddress;
    }

    public void setDeliverAddress(WarehouseEnum deliverAddress) {
        this.deliverAddress = deliverAddress;
    }

    @Column(name = "audit_date")
    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    @Column(name = "finance_date")
    public Date getFinanceDate() {
        return financeDate;
    }

    public void setFinanceDate(Date financeDate) {
        this.financeDate = financeDate;
    }

    @Column(name = "deliver_date")
    public Date getDeliverDate() {
        return deliverDate;
    }

    public void setDeliverDate(Date deliverDate) {
        this.deliverDate = deliverDate;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_user_id")
    public User getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(User auditUser) {
        this.auditUser = auditUser;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_user_id")
    public User getFinanceUser() {
        return financeUser;
    }

    public void setFinanceUser(User financeUser) {
        this.financeUser = financeUser;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliver_user_id")
    public User getDeliverUser() {
        return deliverUser;
    }

    public void setDeliverUser(User deliverUser) {
        this.deliverUser = deliverUser;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_id")
    public User getCreated() {
        return created;
    }

    public void setCreated(User created) {
        this.created = created;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @NotFound(action = NotFoundAction.IGNORE)
    public Order getParent() {
        return parent;
    }

    public void setParent(Order parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    public List<Order> getChildren() {
        return children;
    }

    public void setChildren(List<Order> children) {
        this.children = children;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    public List<OrderProduct> getProducts() {
        return products;
    }

    public void setProducts(List<OrderProduct> products) {
        this.products = products;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    @OrderBy("creationTime desc ")
    public List<OrderProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(List<OrderProcess> processes) {
        this.processes = processes;
    }
}
