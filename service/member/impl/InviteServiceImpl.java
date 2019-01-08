package com.cb.service.member.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.hibernate.query.Page;
import com.cb.dao.member.InviteDao;
import com.cb.model.member.Invite;
import com.cb.model.member.Member;
import com.cb.service.member.InviteService;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/2/14.
 */
@Service
@Transactional
public class InviteServiceImpl extends CommonServiceImpl<Invite, String> implements InviteService {

    @Autowired
    private InviteDao inviteDao;

    @Override
    protected CommonDao<Invite, String> getCommonDao() {
        return inviteDao;//这个返回 ,save时可以调用,否则用service.save会空指针
    }

    @Override
    public void addInvite(String invitee, String inviter) {
        Invite invite = new Invite();

        invite.setInvitee(invitee);
        invite.setInviter(inviter);
        inviteDao.save(invite);
    }

    @Override
    public Page<Member> findPageByParams(Page<Member> page, Map<String, Object> params) {
        return inviteDao.findPageByParams(page, params);
    }

    public List<Member> findListById(String id, Map<String, Object> params) {
        return inviteDao.findListById(params);
    }
}
