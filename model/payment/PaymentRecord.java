package com.cb.model.payment;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import com.cb.model.security.User;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付记录
 */
@Entity
@Table(name = "p_payment_record")
@Where(clause = Constants.DELETED_FALSE)
public class PaymentRecord extends CommonEntity {

    public enum TypeEnum {
        OFFLINE("线下支付"), ALIPAY("支付宝"), WECHAT("微信支付");

        private String desc;

        private TypeEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum StatusEnum {
        SUCCESS("支付成功"),
        REFUND("转入退款"),
        NOTPAY("未支付"),
        CLOSED("已关闭"),
        REVOKED("已撤销（刷卡支付）"),
        USERPAYING("用户支付中"),
        PAYERROR("支付失败(其他原因，如银行返回失败)");

        private String desc;

        private StatusEnum(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    private TypeEnum type;  //类型

    private StatusEnum status;  //状态

    private String code;    //编码

    private String refundCode;  //退款时对应流水

    private String prepayId;    //微信生成的预支付回话标识，用于后续接口调用中使用，该值有效期为2小时

    private String tradeNo;    //微信支付订单编号

    private BigDecimal amount;  //金额

    private String feeType;    //金额类型

    private String tradeType;   //交易类型

    private String ip;  //支付终端ip

    private Date startTime; //支付开始时间

    private Date endTime;//支付失效时间

    private String remark;  //备注

    private User user;  //支付用户

    @Enumerated(EnumType.STRING)
    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    @Enumerated(EnumType.STRING)
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "refund_code")
    public String getRefundCode() {
        return refundCode;
    }

    public void setRefundCode(String refundCode) {
        this.refundCode = refundCode;
    }

    @Column(name = "prepay_id")
    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }

    @Column(name = "trade_no")
    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Column(name = "fee_type")
    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    @Column(name = "trade_type")
    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Column(name = "start_time")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Column(name = "end_time")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
