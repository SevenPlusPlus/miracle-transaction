package com.miracle.common.transaction.api;

import java.util.List;

import lombok.Data;

import com.google.common.collect.Lists;

@Data
public class TccTransaction {
	private Long transactionId;
	
	private Integer status; //TccStatus
	
	private Integer role; //TccRole
	
	private volatile Integer retriedCount = 0; //retried count already
	
	private Long createTime;
	
	private Long lastUpdateTime;
	
	private Integer mode; //TccMode
	
	private Integer version = 1; //version control
	
	private String targetClass;
	
	private String targetMethod;
	
	private List<Participant> participants;
	
	public TccTransaction() //only used for serialization
	{
	}

    public TccTransaction(Long transId) {
        this.transactionId = transId;
        this.createTime = System.currentTimeMillis();
		this.lastUpdateTime = System.currentTimeMillis();
        participants = Lists.newCopyOnWriteArrayList();
    }

    public void registerParticipant(Participant participant) {
        participants.add(participant);
    }
}
