package com.cb.service.member.impl;

import com.cb.common.core.dao.CommonDao;
import com.cb.common.core.service.impl.CommonServiceImpl;
import com.cb.common.hibernate.query.Page;
import com.cb.common.util.*;
import com.cb.common.util.reflection.BeanUtil;
import com.cb.dao.member.MemberDao;
import com.cb.model.member.Invite;
import com.cb.model.member.Member;
import com.cb.model.security.User;
import com.cb.service.member.InviteService;
import com.cb.service.member.MemberService;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by l on 2016/11/28.
 */
@Service
@Transactional
public class MemberServiceImpl extends CommonServiceImpl<Member, String> implements MemberService {

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private InviteService inviteService;

    @Override
    protected CommonDao<Member, String> getCommonDao() {
        return memberDao;
    }

    @Override
    public boolean isExist(String mobile) {
        Member member = memberDao.findUnique(Restrictions.eq("mobile", mobile));
        return member != null;
    }

    @Override
    public Member findByMobile(String mobile) {
        Member member = memberDao.findUnique(Restrictions.eq("mobile", mobile));
        return member;
    }

    @Override
    public Member getByOpenid(String openid) {
        return memberDao.findUnique(Restrictions.eq("openId", openid));
    }

    @Override
    public Map<String, Object> statistics() {
        List<Map<String, Object>> list1 = memberDao.statistics();
        Map<String, Object> result = new LinkedHashMap<>();
        for (Member.LevelEnum level : Member.LevelEnum.values()) {
            result.put(level.getDesc() + "人数", 0);
        }
        for (Map<String, Object> map : list1) {
            Object key = map.get("le");
            Object value = map.get("num");
            for (Member.LevelEnum level : Member.LevelEnum.values()) {
                if (level.name().equals(key)) {
                    result.put(level.getDesc() + "人数", value);
                    break;
                }
            }
        }

        List<Member> list2 = memberDao.find(Restrictions.eq("status", Member.StatusEnum.FAILED));
        result.put("认证未通过人数", list2.size());

        List<Member> list3 = memberDao.find(Restrictions.eq("status", Member.StatusEnum.SUCCESS));
        result.put("通过认证总数", list3.size());

        return result;
    }

    @Override
    public Map<String, Object> statistics(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = memberDao.statistics(params);
        for (Map<String, Object> map : list) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                List<Object> values = (List<Object>) result.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                }
                values.add(entry.getValue());
                result.put(key, values);

            }
        }
        result.put("list", list);

        params.clear();
        params.put("todayStart", DateUtil.dateToString(DateUtil.newInstanceDateBegin(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("todayEnd", DateUtil.dateToString(DateUtil.newInstanceDateEnd(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("weekStart", DateUtil.dateToString(DateUtil.firstDayOfWeek(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("weekEnd", DateUtil.dateToString(DateUtil.lastDayOfWeek(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("monthStart", DateUtil.dateToString(DateUtil.firstDayOfMonth(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        params.put("monthEnd", DateUtil.dateToString(DateUtil.lastDayOfMonth(), DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_HH_MM_SS));
        List<Map<String, Object>> list2 = memberDao.statistics2(params);
        if (ListUtil.isNotEmpty(list2)) {
            List<Map.Entry<String, Object>> sortList = new ArrayList<>(list2.get(0).entrySet());
            Collections.sort(sortList, new Comparator<Map.Entry<String, Object>>() {
                //升序排序
                public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
            Map<String, Object> head = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : sortList) {
                head.put(entry.getKey().substring(1), entry.getValue() == null ? 0 : entry.getValue());
            }
            result.put("head", head);
        }
        return result;
    }

    @Override
    public Member edit(Member member) {
        if (StringUtil.isNotBlank(member.getId())) {
            Member dbMember = memberDao.get(member.getId());

            String diamondId = null;
            switch (member.getLevel()) {
                case GOLD:
                    String agentId = member.getAgent().getId();
                    if (StringUtil.isNotBlank(agentId)) {
                        Member agent = findById(agentId);
                        if (agent != null) {
                            if (agent.getLevel().equals(Member.LevelEnum.DIAMOND)) {
                                diamondId = agent.getId();
                            } else {
                                diamondId = agent.getAgent().getId();
                            }
                        }
                    }
                    break;
                case PLATINUM:
                    diamondId = member.getAgent().getId();
                    break;
                case DIAMOND:
                    diamondId = member.getId();
                    break;
                default:
                    break;
            }
            boolean first = dbMember.getFirst();
            Integer score = dbMember.getScore();
            Integer avScore = dbMember.getAvailableScore();
            Integer unScore = dbMember.getUnavailableScore();
            BeanUtil.copyPropertiesWithoutNullValues(dbMember, member);
            dbMember.setFirst(first);   //copy时将first置为了false，所以需要在此处赋值
            dbMember.setScore(score);
            dbMember.setAvailableScore(avScore);
            dbMember.setUnavailableScore(unScore);
            dbMember.getUser().setUsername(member.getMobile());
            dbMember.getUser().setNickname(member.getMobile());
            //如果没有引荐人
            if (dbMember.getParent() != null && StringUtil.isBlank(dbMember.getParent().getId())) {
                dbMember.setParent(null);
            }
            //如果没有上级代理
            if (dbMember.getAgent() != null && StringUtil.isBlank(dbMember.getAgent().getId())) {
                dbMember.setAgent(null);
            }
            dbMember.setDiamond(diamondId);
            save(dbMember);
        } else {
            //处理合同编码
            Date date = member.getJoinDate();
            int year = DateUtil.getYear(date);
            char head = 'A';
            if (year > 2014) {
                head = (char) ((int) head + year - 2014);
            }
            StringBuilder sb = new StringBuilder("CB");
            sb.append(head);
            String dateStr = DateUtil.dateToString(date, DateUtil.DateFormatter.FORMAT_MMDD);
            sb.append(dateStr);
            String temp = sb.toString();
            member.setContractNumber(temp + StringUtil.randomNumeric(3));
            while (memberDao.findUnique(Restrictions.eq("contractNumber", member.getContractNumber())) != null) {
                member.setContractNumber(temp + StringUtil.randomNumeric(3));
            }
            member.setStatus(Member.StatusEnum.WAITING);

            //关联用户
            User user = new User();
            String salt = StringUtil.getSalt();
            user.setPassword(new SimpleHash(Constants.MD5, "cb888", salt).toString());
            user.setSalt(salt);
            user.setUsername(member.getMobile());
            user.setNickname(member.getMobile());
            user.setType(User.TypeEnum.WECHAT);
            user.setMember(member);
            member.setUser(user);

            //如果没有引荐人
            if (member.getParent() != null && StringUtil.isBlank(member.getParent().getId())) {
                member.setParent(null);
            }
            //如果没有上级代理
            if (member.getAgent() != null && StringUtil.isBlank(member.getAgent().getId())) {
                member.setAgent(null);
            }
            save(member);
        }
        return null;
    }

    @Override
    public Member register(Member member, String parentId, String isInvite) {
        //关联用户
        User user = member.getUser();
        String salt = StringUtil.getSalt();
        user.setPassword(new SimpleHash(Constants.MD5, user.getPassword(), salt).toString());
        user.setSalt(salt);
        user.setUsername(member.getMobile());
        user.setType(User.TypeEnum.WECHAT);
        user.setMember(member);

        //父级
        if (StringUtil.isNotBlank(parentId)) {
            Member parent = memberDao.findUnique(Restrictions.eq("id", parentId));
            if (parent != null) {
                member.setParent(parent);
                //TODO 暂时上级代理人员定义为引荐人
                member.setAgent(parent);
            }
        }
        member.setStatus(Member.StatusEnum.UNSUBMIT);//GuoMIn 修改成为未提交认证资料
        memberDao.save(member);

        if (isInvite != null) {
            inviteService.save(new Invite(parentId, member.getId()));
        }
        return member;
    }

    @Override
    public Page<Member> findPageByParams(Page<Member> page, Map<String, Object> params) {
        return memberDao.findPageByParams(page, params);
    }

    @Override
    public boolean changeStatus(String id, Member.StatusEnum status, String cause) {
        Member member = findById(id);
        if (member != null) {
            member.setStatus(status);
            member.setAuditDate(DateUtil.newInstanceDate());
            member.setAuditUser(ShiroSecurityUtil.getCurrentUser());
            if (Member.StatusEnum.SUCCESS.equals(status)) {
                member.setAuthorizationKey(StringUtil.substringAfter(member.getContractNumber(), "CB"));
                //FIXME  暂时设定授权日期为 授权日至2017年8月31日
                member.setAuthorizationStartTime(DateUtil.newInstanceDate());
                member.setAuthorizationEndTime(DateUtil.parse("2017年8月31日", DateUtil.DateFormatter.FORMAT_YYYY_MM_DD_CN));

                switch (member.getLevel()) {
                    case GOLD:
                        if (member.getAgent().getLevel().equals(Member.LevelEnum.DIAMOND)) {
                            member.setDiamond(member.getAgent().getId());
                        } else {
                            member.setDiamond(member.getAgent().getAgent().getId());
                        }
                        break;
                    case PLATINUM:
                        member.setDiamond(member.getAgent().getId());
                        break;
                    case DIAMOND:
                        member.setDiamond(member.getId());
                        break;
                    default:
                        break;
                }
            } else {
                member.setRejectcause(cause);//审核不通过原因
            }
        }
        return false;
    }

    @Override
    public List<Member> findListByStatus(Member.StatusEnum status) {
        return memberDao.createCriteria(Restrictions.eq("status", status)).addOrder(Order.desc("creationTime")).list();
    }

    @Override
    public List<Member> search(String text) {
        return memberDao.find("from Member where (mobile=? or wechat=? or authorizationKey=?) and status=?", text, text, text, Member.StatusEnum.SUCCESS);
    }

    @Override
    public Member submitInformation(Member oldMmember, Member member) {// 提交资料

        //可优化
        if (oldMmember != null && member != null) {
            oldMmember.setRealName(member.getRealName());//姓名
            oldMmember.setIdCardImgFront(member.getIdCardImgFront());//身份证
            oldMmember.setIdCardImgOpposite(member.getIdCardImgOpposite());
            oldMmember.setIdCard(member.getIdCard());
            oldMmember.setWechat(member.getWechat());
            oldMmember.setJoinDate(member.getJoinDate());
            oldMmember.setLevel(member.getLevel());
            oldMmember.setAddress(member.getAddresses().get(0));//这个地址不存入表中
            oldMmember.setAddresses(member.getAddresses());//地址
            oldMmember.setProvince(member.getProvince());//省份
            oldMmember.setCity(member.getCity());
            oldMmember.setArea(member.getArea());
            oldMmember.setUser(oldMmember.getUser());
//            BeanUtil.copyPropertiesWithoutNullValues(oldMmember, member);
            //处理合同
            Date date = member.getJoinDate();
            int year = DateUtil.getYear(date);
            char head = 'A';
            if (year > 2014) {
                head = (char) ((int) head + year - 2014);
            }
            StringBuilder sb = new StringBuilder("CB");
            sb.append(head);
            String dateStr = DateUtil.dateToString(date, DateUtil.DateFormatter.FORMAT_MMDD);
            sb.append(dateStr);
            String temp = sb.toString();
            String contractNumber = temp + StringUtil.randomNumeric(3);
            while (memberDao.findUnique(Restrictions.eq("contractNumber", contractNumber)) != null) {
                contractNumber = temp + StringUtil.randomNumeric(3);
            }
            oldMmember.setContractNumber(contractNumber);
            oldMmember.setStatus(Member.StatusEnum.VERIFY);//交给引荐人审核
            // TODO: 2017/2/20 金卡会员可享首单免10元活动
            if (oldMmember.getLevel().equals(Member.LevelEnum.GOLD)) {
                oldMmember.setFirst(true);
            }
            if (oldMmember.getLevel().equals(Member.LevelEnum.DIAMOND)) {//钻石不能有代理人
                oldMmember.setAgent(null);
            }
            save(oldMmember);//提交了资料更改状态为等待上级审核
            // 发送模板消息给上级
            WeixinUtil.sendWxBindMember(oldMmember);
        }
        return oldMmember;
    }

    @Override
    public List<Member> findVerifyList() {
        return memberDao.find(Restrictions.eq("parent", ShiroSecurityUtil.getCurrentMember()), Restrictions.eq("status", Member.StatusEnum.VERIFY));
    }

    @Override
    public int batchDelete(String[] ids) {
        return memberDao.batchDelete(ids);
    }

    @Override
    public List<Map<String, Object>> tree(Map<String, Object> params) {
        return memberDao.tree(params);
    }

    @Override
    public List<Map<String, Object>> diamond(String mobile) {
        return memberDao.diamond(mobile);
    }
}
