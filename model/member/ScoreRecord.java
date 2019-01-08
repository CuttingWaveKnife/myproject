package com.cb.model.member;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Created by l on 2017/2/18.
 */
@Entity
@Table(name = "u_member_score_record")
@Where(clause = Constants.DELETED_FALSE)
public class ScoreRecord extends CommonEntity {

    public enum StatusEnum {
        //冻结的积分
        FROZEN,
        //激活的积分
        ACTIVATION
    }

    private String title;

    private Integer score;

    private StatusEnum status;

    private String remark;

    private Member member;

    public ScoreRecord() {
    }

    public ScoreRecord(String title, Integer score, StatusEnum status, String remark, Member member) {
        this.title = title;
        this.score = score;
        this.status = status;
        this.remark = remark;
        this.member = member;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Enumerated(EnumType.STRING)
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
