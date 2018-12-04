package com.miracle.common.transaction.rpc.aop;

import com.miracle.common.transaction.tcc.aop.AbstractTccTransactionAspect;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;



@Aspect
@Component
public class RpcTccTransactionAspect extends AbstractTccTransactionAspect implements Ordered{

	@Autowired
	public RpcTccTransactionAspect(RpcTccTransactionInterceptor kkrpcTccTransactionInterceptor)
	{
		super.setTccTransactionInterceptor(kkrpcTccTransactionInterceptor);
	}
	
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
