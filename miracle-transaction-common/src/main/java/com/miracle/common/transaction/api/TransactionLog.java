package com.miracle.common.transaction.api;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionLog implements Serializable{
	
	private static final long serialVersionUID = 6332922516227889706L;
	
	private String className;
	
	private String methodName;

	private Map<String, Object> paramMap;
	
	private Map<String, String> attachments;
	
}
