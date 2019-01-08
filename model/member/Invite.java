package com.cb.model.member;

import com.cb.common.core.model.CommonEntity;
import com.cb.common.util.Constants;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by GuoMIn on 2017/2/14.
 */
@Entity
@Table(name = "u_invite")
@Where(clause = Constants.DELETED_FALSE)
public class Invite extends CommonEntity {

    private String inviter;//邀请者

    private String invitee;//被邀请者

    public Invite() {
    }

    public Invite(String inviter, String invitee) {
        this.inviter = inviter;
        this.invitee = invitee;
    }

    public void setInvitee(String invitee) {
        this.invitee = invitee;
    }

    @Column(name = "invitee_id")
    public String getInvitee() {
        return invitee;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    @Column(name = "inviter_id", unique = true)
    public String getInviter() {
        return inviter;
    }
}
