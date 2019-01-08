package com.cb.model.member;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import com.cb.common.util.DateUtil;
import com.cb.common.util.StringUtil;
import com.cb.model.security.User;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


/**
 * 会员
 */
@Entity
@Table(name = "u_member")
@Where(clause = Constants.DELETED_FALSE)
public class Member extends CommonEntity {

    public enum LevelEnum {
        FAMILY("亲情会员", "#b8914d"),
        GOLD("金卡精英会员", "#131313"),
        PLATINUM("白金精英会员", "#131313"),
        DIAMOND("钻石精英会员", "#b8914d");

        private String desc;

        private String color;

        private LevelEnum(String desc, String color) {
            this.desc = desc;
            this.color = color;
        }

        public String getDesc() {
            return desc;
        }

        public String getColor() {
            return color;
        }
    }

    public enum StatusEnum {
        FAILED("未通过审核"), UNSUBMIT("未提交审核"), VERIFY("上级审核"), WAITING("等待审核"), SUCCESS("通过审核");

        private String desc;

        private StatusEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    private String realName;    //真实姓名

    private String rejectcause;    //审核备注

    private Short sex;  //性别

    private Short age;  //年龄

    private Date birthday;  //生日

    private String head;    //头像

    private String mobile;  //手机号码

    private String telephone;   //电话号码

    private String province;    //所在省份

    private String city;    //所在城市

    private String area;    //所在地区

    private String idCard;  //身份证

    private String idCardImgFront;     //身份证正面照片

    private String idCardImgOpposite;   //身份证反面照片

    private String wechat;  //微信帐号

    private String openId; //微信opend_id

    private String shopUrl; //淘宝店铺链接

    private LevelEnum level;    //等级

    private StatusEnum status;   //审核状态（-1：未通过；0：未审核；1：通过审核）

    private String contractNumber;  //合同编号

    private Date joinDate;   //加入日期

    private String authorizationKey;    //授权码

    private Date authorizationStartTime;    //授权开始日期

    private Date authorizationEndTime;   //授权结束日期

    private Date auditDate; //审核时间

    private Integer score = 0;  //积分

    private Integer availableScore = 0; //可用积分

    private Integer unavailableScore = 0;   //不可用积分（正在发放的积分）

    // TODO: 2017/2/20 首次下单优惠暂时写法
    private Boolean first = false;  //首次下单优惠

    private String diamond; //直系上属钻石会员(如果会员是钻石，则该字段关联其本身)

    private String code;    //对应金蝶系统的客户编号

    private User auditUser; //审核人

    private User user;  //对应系统用户帐号

    private Member parent;  //引荐人

    private List<Member> children;  //引荐会员集合

    private Member agent;   //代理人

    private List<Member> subordinate; //下级会员集合

    private List<Address> addresses;    //会员地址库

    private List<ScoreRecord> scoreRecords; //积分清单

    private String datepicker;  //授权日期拼接字符串

    private Address address;    //会员默认地址

    public String getRejectcause() {
        return rejectcause;
    }

    public void setRejectcause(String rejectcause) {
        this.rejectcause = rejectcause;
    }

    @Column(name = "real_name", length = 10)
    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    @Column(length = 1)
    public Short getSex() {
        return sex;
    }

    public void setSex(Short sex) {
        this.sex = sex;
    }

    @Column(length = 3)
    public Short getAge() {
        return age;
    }

    public void setAge(Short age) {
        this.age = age;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    @Column(nullable = false, length = 11)
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Column(length = 15)
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Column(name = "id_card")
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    @Column(name = "id_card_img_front")
    public String getIdCardImgFront() {
        return idCardImgFront;
    }

    public void setIdCardImgFront(String idCardImgFront) {
        this.idCardImgFront = idCardImgFront;
    }

    @Column(name = "id_card_img_opposite")
    public String getIdCardImgOpposite() {
        return idCardImgOpposite;
    }

    public void setIdCardImgOpposite(String idCardImgOpposite) {
        this.idCardImgOpposite = idCardImgOpposite;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    @Column(name = "open_id")
    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    @Column(name = "shop_url")
    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    @Enumerated(value = EnumType.STRING)
    public LevelEnum getLevel() {
        return level;
    }

    public void setLevel(LevelEnum level) {
        this.level = level;
    }

    @Enumerated(value = EnumType.STRING)
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    @Column(name = "contract_number")
    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    @Column(name = "join_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    @Column(name = "authorization_key")
    public String getAuthorizationKey() {
        return authorizationKey;
    }

    public void setAuthorizationKey(String authorizationKey) {
        this.authorizationKey = authorizationKey;
    }

    @Column(name = "authorization_start_time")
    public Date getAuthorizationStartTime() {
        return authorizationStartTime;
    }

    public void setAuthorizationStartTime(Date authorizationStartTime) {
        this.authorizationStartTime = authorizationStartTime;
    }

    @Column(name = "authorization_end_time")
    public Date getAuthorizationEndTime() {
        return authorizationEndTime;
    }

    public void setAuthorizationEndTime(Date authorizationEndTime) {
        this.authorizationEndTime = authorizationEndTime;
    }

    @Column(name = "audit_date")
    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Column(name = "available_score")
    public Integer getAvailableScore() {
        return availableScore;
    }

    public void setAvailableScore(Integer availableScore) {
        this.availableScore = availableScore;
    }

    @Column(name = "unavailable_score")
    public Integer getUnavailableScore() {
        return unavailableScore;
    }

    public void setUnavailableScore(Integer unavailableScore) {
        this.unavailableScore = unavailableScore;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    @Column(name = "diamond_id")
    public String getDiamond() {
        return diamond;
    }

    public void setDiamond(String diamond) {
        this.diamond = diamond;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_user_id")
    public User getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(User auditUser) {
        this.auditUser = auditUser;
    }

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @NotFound(action = NotFoundAction.IGNORE)
    public Member getParent() {
        return parent;
    }

    public void setParent(Member parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    public List<Member> getChildren() {
        return children;
    }

    public void setChildren(List<Member> children) {
        this.children = children;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    @NotFound(action = NotFoundAction.IGNORE)
    public Member getAgent() {
        return agent;
    }

    public void setAgent(Member agent) {
        this.agent = agent;
    }

    @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)
    public List<Member> getSubordinate() {
        return subordinate;
    }

    public void setSubordinate(List<Member> subordinate) {
        this.subordinate = subordinate;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OrderBy("creationTime desc")
    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @OrderBy("creationTime desc")
    @Where(clause = "status='ACTIVATION'")
    public List<ScoreRecord> getScoreRecords() {
        return scoreRecords;
    }

    public void setScoreRecords(List<ScoreRecord> scoreRecords) {
        this.scoreRecords = scoreRecords;
    }

    @Transient
    public String getDatepicker() {
        if (this.getAuthorizationStartTime() != null && this.getAuthorizationEndTime() != null) {
            return DateUtil.dateToString(this.getAuthorizationStartTime(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD) + " - " + DateUtil.dateToString(this.getAuthorizationEndTime(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD);
        }
        return this.datepicker;
    }

    public void setDatepicker(String datepicker) {
        if (StringUtil.isNotBlank(datepicker)) {
            String[] temps = datepicker.split(" - ");
            if (temps.length == 2) {
                this.setAuthorizationStartTime(DateUtil.parse(temps[0], DateUtil.DateFormatter.FORMAT_YYYY_MM_DD));
                this.setAuthorizationEndTime(DateUtil.parse(temps[1], DateUtil.DateFormatter.FORMAT_YYYY_MM_DD));
                this.datepicker = datepicker;
            }
        }
    }

    @Transient
    public Address getAddress() {
        if (address == null) {
            List<Address> addresses = getAddresses();
            for (Address address : addresses) {
                if (address.isDef()) {
                    setAddress(address);
                    break;
                }
            }
        }
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
