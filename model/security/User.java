package com.cb.model.security;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import com.cb.model.member.Member;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * 用户
 */
@Entity
@Table(name = "s_user")
@Where(clause = Constants.DELETED_FALSE)
public class User extends CommonEntity {

    public enum TypeEnum {
        //系统用户
        SYSTEM,
        //微信用户
        WECHAT,
        //开放接口用户
        OPEN
    }

    public enum StatusEnum {
        //正常
        ENABLED,
        //已注销
        CANCELED,
        //已冻结
        FROZEN,
        //有异常
        ABNORMALITY
    }

    private TypeEnum type;  //帐号类型

    private String username;   //用户名

    private String password;    //登录密码

    private String salt;    //密码佐料

    private StatusEnum status = StatusEnum.ENABLED;   //帐号状态

    private String nickname;    //昵称

    private Member member;  //对应会员帐号

    private List<Role> roles;   //用户角色

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "s_user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
