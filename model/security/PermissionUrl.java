package com.cb.model.security;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * shiro权限对应链接
 */
@Entity
@Table(name = "s_permission_url")
@Where(clause = Constants.DELETED_FALSE)
public class PermissionUrl extends CommonEntity {

    private String name;    //链接描述

    private String url;     //链接

    private Integer sort;   //排序

    private Permission permission;  //对应权限

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "permission_id")
    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
