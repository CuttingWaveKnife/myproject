package com.cb.model.active;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by GuoMIn on 2017/3/23.
 */
@Entity
@Table(name = "a_active_record")
@Where(clause = Constants.DELETED_FALSE)
public class Record extends CommonEntity {

    public enum StatusEnum {
        YES("已参与"),
        CANNOT("未参与");

        private String desc;

        private StatusEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    private StatusEnum status;

    private String memberId;//会员ID

    private String code;//对应订单编号

    private String imgUrl;//图片地址

    private Integer number = 0;//可购买数量

    private Integer praiseNumber = 0;//可购买数量

    private Date auditDate; //客服审核时间

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setPraiseNumber(Integer praiseNumber) {
        this.praiseNumber = praiseNumber;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Column(name = "praise_number")
    public Integer getPraiseNumber() {
        return praiseNumber;
    }

    @Column(name = "img_url")
    public String getImgUrl() {
        return imgUrl;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    @Column(name = "member_id")
    public String getMemberId() {
        return memberId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Column(name = "audit_date")
    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }
}
