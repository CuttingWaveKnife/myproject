package com.cb.vo;

import com.cb.model.member.Member;

import java.math.BigDecimal;

/**
 * @author yangjin 2017/5/22
 */
public class Info {

    private String id;

    private String name;

    private String level;

    private BigDecimal money;

    private Integer team = 0;

    private BigDecimal all = BigDecimal.ZERO;

    private BigDecimal teamMoney = BigDecimal.ZERO;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = Member.LevelEnum.valueOf(level).getDesc().substring(0,2);
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Integer getTeam() {
        return team;
    }

    public void setTeam(Integer team) {
        this.team = team;
    }

    public BigDecimal getAll() {
        return all;
    }

    public void setAll(BigDecimal all) {
        this.all = all;
    }

    public BigDecimal getTeamMoney() {
        return teamMoney;
    }

    public void setTeamMoney(BigDecimal teamMoney) {
        this.teamMoney = teamMoney;
    }
}
