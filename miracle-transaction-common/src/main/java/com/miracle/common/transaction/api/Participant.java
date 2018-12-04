package com.miracle.common.transaction.api;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Participant implements Serializable{

	private static final long serialVersionUID = -5800542467865474915L;
	
	private long transactionId;
	
	private TccInvocation confirmTccInvocation;
	
	private TccInvocation cancelTccInvocation;
	
	public Participant()
	{
		//default constructor
	}
}
