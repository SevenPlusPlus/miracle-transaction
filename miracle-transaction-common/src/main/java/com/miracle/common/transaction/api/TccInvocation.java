package com.miracle.common.transaction.api;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@SuppressWarnings("rawtypes")
public class TccInvocation implements Serializable {

	private static final long serialVersionUID = -3589474155438476489L;

	private Class targetClass;

	private String methodName;

	private Class[] parameterTypes;

	private Object[] args;
	
	private Map<String, String> attachments;
	
	public TccInvocation() {
		//default constructor
	}
}
