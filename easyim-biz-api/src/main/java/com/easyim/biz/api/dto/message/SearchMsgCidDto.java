package com.easyim.biz.api.dto.message;

import java.io.Serializable;

import lombok.Data;

@Data
public class SearchMsgCidDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6624765696319024427L;
	private long tenementId;
	private long cid;
	private String userId;
	private long minMsgId;
}
