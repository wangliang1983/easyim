package com.easyim.biz.mapper.tenement;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.easyim.biz.domain.TenementDo;

@Mapper
public interface ITenementMapper {
	/**
	 * 查询相关租户
	 * @param id
	 * @return
	 */
	@Select("select * from t_tenement where id=#{id}")
	@Results(
    		id="tenement",
    		value={
    				@Result(column="id",property="id",id=true),
    				@Result(column="name",property="name"),
    				@Result(column="is_multi_conn",property="isMultiConn"),
    				@Result(column="biz_code",property="bizCode"),
    				@Result(column="gmt_create",property="gmtCreate"),
    		}
    		)
	public TenementDo getTenementById(@Param("id") long id);
}
