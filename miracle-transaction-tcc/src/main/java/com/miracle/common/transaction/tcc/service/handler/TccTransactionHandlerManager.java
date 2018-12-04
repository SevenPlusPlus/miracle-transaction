package com.miracle.common.transaction.tcc.service.handler;

import com.miracle.common.transaction.api.TccTransactionContext;
import com.miracle.common.transaction.exception.TccRuntimeException;
import com.miracle.common.transaction.tcc.TccTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle.common.transaction.annotation.api.Propagation;


@Component
public class TccTransactionHandlerManager {
	
	@Autowired
	private TccTransactionManager tccTransManager;

	@Autowired
	private StarterTccTransactionHandler starterTccHandler;
	
	@Autowired
	private ProviderTccTransactionHandler providerTccHandler;
	
	@Autowired
	private NormalTccTransactionHandler normalTccHandler;
	
	public TccTransactionHandler getTccTransactionHandlerByTccContext(Propagation propagation, TccTransactionContext context)
	{
	    /*
	     * 开启事务了,则根据当前线程变量是否有值和是否开启了事务判断属于start或是远程调用还是发起远程调用的方法。
	     */
		if(context == null)
		{
			if((propagation == Propagation.PROPAGATION_REQUIRED) && !tccTransManager.isBegin()) {
				return starterTccHandler;
			}
			if(propagation == Propagation.PROPAGATION_MANDATORY)
			{
				throw new TccRuntimeException("no active tcc transaction context while propagation is mandatory");
			}
		}
		else if(context != null)
		{
			if((propagation == Propagation.PROPAGATION_REQUIRED) || 
					(propagation == Propagation.PROPAGATION_SUPPORTS) ||
					(propagation == Propagation.PROPAGATION_MANDATORY) 
					)
			{
				return providerTccHandler;
			}
		}
		return normalTccHandler;
	}
}
