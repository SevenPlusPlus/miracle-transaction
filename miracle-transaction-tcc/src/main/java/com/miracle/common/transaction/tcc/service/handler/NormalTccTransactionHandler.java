package com.miracle.common.transaction.tcc.service.handler;

import com.miracle.common.transaction.api.TccTransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;


@Component
public class NormalTccTransactionHandler implements TccTransactionHandler{

	@Override
	public Object handler(ProceedingJoinPoint pjp,
			TccTransactionContext tccTransactionContext) throws Throwable {
		return pjp.proceed();
	}

}
