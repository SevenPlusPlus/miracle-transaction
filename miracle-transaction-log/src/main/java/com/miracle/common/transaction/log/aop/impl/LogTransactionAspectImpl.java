package com.miracle.common.transaction.log.aop.impl;

import com.miracle.common.transaction.log.aop.AbstractLogTransactionAspect;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class LogTransactionAspectImpl extends AbstractLogTransactionAspect {

	@Autowired
	public LogTransactionAspectImpl(LogTransactionInterceptorImpl logTransactionInterceptor)
	{
		super.setLogTransactionInterceptor(logTransactionInterceptor);
	}
	
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
	
}
