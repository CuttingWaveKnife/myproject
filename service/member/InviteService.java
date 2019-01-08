package com.cb.service.member;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.member.Invite;
import com.cb.model.member.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/2/14.
 */
public interface InviteService extends CommonService<Invite, String> {

    void addInvite(String invitee, String inviter);

    Page<Member> findPageByParams(Page<Member> page, Map<String, Object> params);

    List<Member> findListById(String id, Map<String, Object> params);

}
