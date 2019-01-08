package com.cb.model.member;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 会员地址信息
 */
@Entity
@Table(name = "u_address")
@Where(clause = Constants.DELETED_FALSE)
public class Address extends CommonEntity {

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

    private boolean def;    //是否设为默认

    private Member member;  //对应会员

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

    public boolean isDef() {
        return def;
    }

    public void setDef(boolean def) {
        this.def = def;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
