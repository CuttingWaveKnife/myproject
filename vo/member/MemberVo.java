package com.cb.vo.member;

import com.cb.common.hibernate.query.Page;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.member.Member;
import com.cb.vo.PageVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MemberVo {

    private Boolean first;                        //first

    private Integer availableScore;                //可用积分

    private MemberLevel level;                    //等级

    private String realName;                    //名称

    private String head;    //头像

    private String mobile;  //手机号码

    private String province;    //所在省份

    private String city;    //所在城市

    private String area;    //所在地区

    private String idCardImgFront;     //身份证正面照片

    private String idCardImgOpposite;   //身份证反面照片

    private String wechat;  //微信帐号
    
    private String idCard;  //身份证

    private String authorizationKey;    //授权码

    private Date authorizationStartTime;    //授权开始日期

    private Date authorizationEndTime;   //授权结束日期

    public static MemberVo toMemberVo(Member member) {
        MemberVo memberVo = new MemberVo();
        BeanUtil.copyPropertiesWithoutNullValues(memberVo, member);
        //额外处理
        if (member.getLevel() != null) {
            MemberLevel memberLevel = memberVo.new MemberLevel();
            memberLevel.setLevel(member.getLevel().name());
            memberLevel.setDesc(member.getLevel().getDesc());
            memberLevel.setColor(member.getLevel().getColor());
            memberVo.setLevel(memberLevel);
        }
        return memberVo;
    }

    public static PageVo<MemberVo> toPageVo(Page<Member> page) {
        PageVo<MemberVo> pageVo = new PageVo<>();
        BeanUtil.copyPropertiesWithoutNullValues(pageVo, page);
        List<MemberVo> list = new ArrayList<>(page.getResult().size());
        for (Member member : page.getResult()) {
            MemberVo memberVo = toMemberVo(member);
            list.add(memberVo);
        }
        pageVo.setList(list);
        return pageVo;
    }

    private class MemberLevel {

        private String level;

        private String desc;

        private String color;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRealName() {
        return realName;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Integer getAvailableScore() {
        return availableScore;
    }

    public void setAvailableScore(Integer availableScore) {
        this.availableScore = availableScore;
    }

    public MemberLevel getLevel() {
        return level;
    }

    public void setLevel(MemberLevel level) {
        this.level = level;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
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

    public String getIdCardImgFront() {
        return idCardImgFront;
    }

    public void setIdCardImgFront(String idCardImgFront) {
        this.idCardImgFront = idCardImgFront;
    }

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

    public String getAuthorizationKey() {
        return authorizationKey;
    }

    public void setAuthorizationKey(String authorizationKey) {
        this.authorizationKey = authorizationKey;
    }

    public Date getAuthorizationStartTime() {
        return authorizationStartTime;
    }

    public void setAuthorizationStartTime(Date authorizationStartTime) {
        this.authorizationStartTime = authorizationStartTime;
    }

    public Date getAuthorizationEndTime() {
        return authorizationEndTime;
    }

    public void setAuthorizationEndTime(Date authorizationEndTime) {
        this.authorizationEndTime = authorizationEndTime;
    }

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
}
