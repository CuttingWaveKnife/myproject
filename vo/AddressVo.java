package com.cb.vo;

import java.io.Serializable;

/**
 * Created by l on 2016/12/1.
 */
public class AddressVo implements Serializable {

    private String id; //地址ID

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

    private Boolean def;    //是否设为默认

    private String warehouse;   //对应仓库

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public Boolean getDef() {
        return def;
    }

    public void setDef(Boolean def) {
        this.def = def;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }
}
