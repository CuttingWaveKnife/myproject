package com.cb.dao.member;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.StringUtil;
import com.cb.model.member.Invite;
import com.cb.model.member.Member;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by GuoMIn on 2017/2/14.
 */
@Repository
public class InviteDao extends CommonDao<Invite, String> {

    public Page<Member> findPageByParams(Page<Member> page, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder("SELECT um.* FROM u_invite ui , u_member um WHERE um.status = 'SUCCESS' AND ui.deleted = 0 AND ui.invitee_id = um.id ");
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (StringUtil.isNotBlank(entry.getKey()) && StringUtil.isNotBlank(entry.getValue())) {
                    if (entry.getKey().equals("search")) {
                        sb.append(" AND (inviter_id LIKE '");
                        sb.append(entry.getValue());
                        sb.append("%' OR invitee_id LIKE '");
                        sb.append(entry.getValue());
                        sb.append("%')");
                    } else {
                        sb.append(" AND ");
                        sb.append(entry.getKey());
                        sb.append(" = :");
                        sb.append(entry.getKey());
                    }
                }
            }
        }
        sb.append(" ORDER BY um.audit_date DESC");
        return (Page<Member>) findPageBySql(page, sb.toString(), Member.class, params);
    }

    public List<Member> findListById( Map<String ,Object> params) {
        StringBuilder sb = new StringBuilder("SELECT um.* FROM u_invite ui , u_member um WHERE um.status = 'SUCCESS' AND ui.deleted = 0 AND ui.invitee_id = um.id ");
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (StringUtil.isNotBlank(entry.getKey()) && StringUtil.isNotBlank(entry.getValue())) {
                    if (entry.getKey().equals("search")) {
                        sb.append(" AND (inviter_id LIKE '");
                        sb.append(entry.getValue());
                        sb.append("%' OR invitee_id LIKE '");
                        sb.append(entry.getValue());
                        sb.append("%')");
                    } else {
                        sb.append(" AND ");
                        sb.append(entry.getKey());
                        sb.append(" = :");
                        sb.append(entry.getKey());
                    }
                }
            }
        }
        sb.append(" ORDER BY um.audit_date DESC");
        return (List<Member>)query(sb.toString() ,Member.class ,params);
    }
}
