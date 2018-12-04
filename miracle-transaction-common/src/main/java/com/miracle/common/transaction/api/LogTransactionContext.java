package com.miracle.common.transaction.api;

import java.io.Serializable;

import lombok.Data;

@Data
public class LogTransactionContext implements Serializable{

	private static final long serialVersionUID = -1306594445048836002L;
	
	private String transOrderId;
	
	private String transEventSeqId;
}
