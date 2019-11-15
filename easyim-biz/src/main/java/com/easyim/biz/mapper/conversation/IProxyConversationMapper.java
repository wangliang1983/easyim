package com.easyim.biz.mapper.conversation;

import java.util.Date;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

import com.easyim.biz.domain.ConversationDo;
import com.easyim.biz.domain.ProxyConversationDo;

@Mapper
public interface IProxyConversationMapper {
	
	@Select("select * from t_proxy_conversation where tenement_id = #{tenementId} and proxy_small_id=#{proxySmallId} and proxy_big_id=#{proxyBigId}")
	@Results(
    		id="proxyConversation",
    		value={
    				@Result(column="id",property="id",id=true),
    				@Result(column="tenement_id",property="tenementId"),
    				@Result(column="proxy_small_id",property="proxySmallId"),
    				@Result(column="proxy_big_id",property="proxyBigId"),
    				@Result(column="gmt_create",property="gmtCreate"),
    		}
    		)
	public ProxyConversationDo getProxyConversation(
			@Param("tenementId") long tenementId,
			@Param("proxySmallId") String proxySmallId,
			@Param("proxyBigId") String proxyBigId);
	
	
	
	@Insert("insert into t_proxy_conversation (tenement_id,proxy_small_id,proxy_big_id) values (#{proxy.tenementId},#{proxy.proxySmallId},#{proxy.proxyBigId})")
	@Options(useGeneratedKeys = true,keyProperty="id",keyColumn="id") // Adding this line instread of @SelectKey 
	public long insertProxyConversationDo(@Param("proxy")ProxyConversationDo cDo);


	@Select( " select count(*) from ((select c.* from t_conversation c "
			+" inner join t_proxy_conversation p_c on c.proxy_cid=p_c.id "
			+" where c.tenement_id=#{tenementId} and c.small_id=#{userId} limit 1) "
			+" union all "
			+" (select c.* from t_conversation c "
			+" inner join t_proxy_conversation p_c on c.proxy_cid=p_c.id "
			+" where c.tenement_id=#{tenementId} and c.big_id=#{userId} limit 1)) as temp ")
	public long selectConversationCount(
			@Param("tenementId") long tenementId,
			@Param("userId") String userId);
}
