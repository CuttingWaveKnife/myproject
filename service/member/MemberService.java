package com.cb.service.member;

import com.cb.common.core.service.CommonService;
import com.cb.common.hibernate.query.Page;
import com.cb.model.member.Member;

import java.util.List;
import java.util.Map;

/**
 * Created by l on 2016/11/28.
 */
public interface MemberService extends CommonService<Member, String> {

    /**
     * 根据手机号码判断会员是否存在
     *
     * @param mobile 手机号码
     * @return 判断结果
     */
    boolean isExist(String mobile);

    /**
     * 根据手机号码查询会员
     *
     * @param mobile 手机号码
     * @return 判断结果
     */
    Member findByMobile(String mobile);

    /**
     * 根据会员openid查询
     *
     * @param openid 微信openid
     * @return 返回会员
     */
    Member getByOpenid(String openid);

    /**
     * 统计各会员等级的人数
     *
     * @return 返回统计信息
     */
    Map<String, Object> statistics();

    /**
     * 根据条件统计各会员等级的人数
     *
     * @param params 查询条件
     * @return 返回统计信息
     */
    Map<String, Object> statistics(Map<String, Object> params);

    /**
     * 后台系统编辑会员
     *
     * @param member 编辑后的会员
     * @return 保存后的会员
     */
    Member edit(Member member);

    /**
     * 微信端注册会员
     *
     * @param member   会员信息
     * @param parentId 父级会员id
     * @return 保存后的会员
     */
    Member register(Member member, String parentId, String isInvite);

    /**
     * 通过条件分页查询会员
     *
     * @param page   分页条件
     * @param params 查询条件
     * @return
     */
    Page<Member> findPageByParams(Page<Member> page, Map<String, Object> params);

    /**
     * 变更会员状态
     *
     * @param id     会员id
     * @param status 状态（通过审核、不通过审核）
     * @return 返回变更是否成功
     */
    boolean changeStatus(String id, Member.StatusEnum status, String cause);

    /**
     * 根据会员状态查询会员集合
     *
     * @param status 状态
     * @return 查询结果
     */
    List<Member> findListByStatus(Member.StatusEnum status);

    /**
     * 根据查询条件查询会员集合
     *
     * @param text 查询条件
     * @return 查询结果
     */
    List<Member> search(String text);

    /**
     * 提交会员认证资料
     *
     * @param oldMember 修改之前会员信息
     * @param member    会员信息
     * @return 提交后会员资料
     */
    Member submitInformation(Member oldMember, Member member);

    /**
     * 查询待当前会员的确认的所有认证会员
     */
    List<Member> findVerifyList();

    /**
     * 查询会员树（只支持钻石会员）
     *
     * @param params 查询条件
     * @return 返回所有数据
     */
    List<Map<String, Object>> tree(Map<String, Object> params);

    /**
     * 钻石引荐关系
     *
     * @param mobile 钻石手机
     * @return 返回所有数据
     */
    List<Map<String, Object>> diamond(String mobile);
}
