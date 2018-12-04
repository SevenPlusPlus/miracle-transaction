package com.miracle.common.transaction.api;

import java.io.Serializable;

import lombok.Data;

@Data
public class TccTransactionContext implements Serializable{

	private static final long serialVersionUID = -6791456558035124174L;

	private Long transactionId;
	
	private int status; /*TccStatus*/
	
	private int role; /*TccRole*/
}
