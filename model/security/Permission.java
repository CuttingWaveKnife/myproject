package com.cb.model.security;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * 权限
 */
@Entity
@Table(name = "s_permission")
@Where(clause = Constants.DELETED_FALSE)
public class Permission extends CommonEntity {

    public enum TypeEnum {
        /*
        anon:  例子/admins/**=anon 没有参数，表示可以匿名使用。
        authc: 例如/admins/user/**=authc表示需要认证(登录)才能使用，没有参数
        authcBasic：例如/admins/user/**=authcBasic没有参数表示httpBasic认证
        user:例如/admins/user/**=user没有参数表示必须存在用户，当登入操作时不做检查

        roles：例子/admins/user/**=roles[admin],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，当有多个参数时，例如admins/user/**=roles["admin,guest"],每个参数通过才算通过，相当于hasAllRoles()方法。
        perms：例子/admins/user/**=perms[user:add:*],参数可以写多个，多个时必须加上引号，并且参数之间用逗号分割，例如/admins/user/**=perms["user:add:*,user:modify:*"]，当有多个参数时必须每个参数都通过才通过，想当于isPermitedAll()方法。
        rest：例子/admins/user/**=rest[user],根据请求的方法，相当于/admins/user/**=perms[user:method] ,其中method为post，get，delete等。
        ssl:例子/admins/user/**=ssl没有参数，表示安全的url请求，协议为https
        port：例子/admins/user/**=port[8081],当请求的url的端口不是8081是跳转到schemal://serverName:8081?queryString,其中schmal是协议http或https等，serverName是你访问的host,8081是url配置里port的端口，queryString
        是你访问的url里的？后面的参数。

        注：anon，authcBasic，auchc，user是认证过滤器，
            perms，roles，ssl，rest，port是授权过滤器
        */
        none("{0}"), perms("perms[{0}]"), roles("roles[{0}]");

        private String replaceString;   //替换权限字符串,例如：权限member:list，通过替换perms[{0}]之后成为：perms[member:list]

        private TypeEnum(String replaceString) {
            this.replaceString = replaceString;
        }

        public String getReplaceString() {
            return replaceString;
        }
    }

    private TypeEnum type;  //shiro权限类型

    private String name;    //权限描述

    private String permission;  //权限

    private Integer sort;   //排序

    private List<Role> roles;   //所属角色

    private List<PermissionUrl> permissionUrls; //shiro权限对应所有链接

    @Enumerated(value = EnumType.ORDINAL)
    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @OneToMany(mappedBy = "permission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("sort desc")
    public List<PermissionUrl> getPermissionUrls() {
        return permissionUrls;
    }

    public void setPermissionUrls(List<PermissionUrl> permissionUrls) {
        this.permissionUrls = permissionUrls;
    }
}
