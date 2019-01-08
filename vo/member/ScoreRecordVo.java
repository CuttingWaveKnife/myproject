package com.cb.vo.member;

import com.cb.common.hibernate.query.Page;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.model.member.ScoreRecord;
import com.cb.vo.PageVo;

import java.util.ArrayList;
import java.util.List;

public class ScoreRecordVo {


    private String title;   //标题

    private Integer score;  //积分

    private String status;  //状态（FROZEN:冻结的积分；ACTIVATION:激活的积分）

    private String remark;  //备注

    public static ScoreRecordVo toMemberVo(ScoreRecord scoreRecord) {
        ScoreRecordVo scoreRecordVo = new ScoreRecordVo();
        BeanUtil.copyPropertiesWithoutNullValues(scoreRecordVo, scoreRecord);
        return scoreRecordVo;
    }

    public static PageVo<ScoreRecordVo> toPageVo(Page<ScoreRecord> page) {
        PageVo<ScoreRecordVo> pageVo = new PageVo<>();
        BeanUtil.copyPropertiesWithoutNullValues(pageVo, page);
        List<ScoreRecordVo> list = new ArrayList<>(page.getResult().size());
        for (ScoreRecord scoreRecord : page.getResult()) {
            ScoreRecordVo scoreRecordVo = toMemberVo(scoreRecord);
            list.add(scoreRecordVo);
        }
        pageVo.setList(list);
        return pageVo;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
