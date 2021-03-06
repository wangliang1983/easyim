package com.easyim.biz.api.dto.protocol;

import java.io.Serializable;
import java.util.UUID;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.easyim.biz.api.protocol.c2s.AbstractProtocol;
import com.easyim.biz.api.protocol.enums.c2s.EasyImC2sType;
import com.easyim.biz.api.protocol.enums.c2s.C2sType;

import lombok.Builder;
import lombok.Data;


@Data
public class C2sProtocol implements Serializable{
	
	private static final long serialVersionUID = 9159509431662007255L;
	
	private  String uuid = UUID.randomUUID().toString();
	private  String version = "1.0";
	@Protobuf
	private  String type;
	@Protobuf
	private  String body;
	
	
	
	public C2sProtocol(){
		
	}
	
	public C2sProtocol(C2sType type){
		this.type = type.getValue();
	}
	
	public C2sProtocol(C2sType type,String body){
		this.type = type.getValue();
		this.body = body;
	}

	
}
