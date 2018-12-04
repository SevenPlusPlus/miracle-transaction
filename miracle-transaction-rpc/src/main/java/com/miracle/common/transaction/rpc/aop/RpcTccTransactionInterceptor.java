package com.miracle.common.transaction.rpc.aop;

import com.miracle.common.transaction.api.TccTransactionContext;
import com.miracle.common.transaction.api.TccTransactionContextLocal;
import com.miracle.common.transaction.tcc.aop.TccTransactionInterceptor;
import com.miracle.common.transaction.tcc.service.TccTransactionAspectService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class RpcTccTransactionInterceptor implements TccTransactionInterceptor {

	@Autowired
	private TccTransactionAspectService tccTransactionAspectService;
	
	@Override
	public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
		TccTransactionContext tccTransactionContext = TccTransactionContextLocal.getInstance().get();
		return tccTransactionAspectService.invoke(tccTransactionContext, pjp);
	}

}
