package com.miracle.common.transaction.tcc.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * AOP order is
 * @Before TccTransactionAspect
 * @Before TccResourceCoordinatorAspect
 * @After TccResourceCoordinatorAspect
 * @After TccTransactionAspect
 * @author wqj
 *
 */

@Aspect
@Component
public class TccResourceCoordinatorLocalAspect implements Ordered{

	@Autowired
	private TccResourceCoordinatorLocalInterceptor tccResourceCoordinatorInterceptor;
	

    @Pointcut("@annotation(com.miracle.common.transaction.annotation.TccTransactional)")
    public void resourceCoordinatorMethod() {

    }

    @Around("resourceCoordinatorMethod()")
    public Object interceptCompensableMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return tccResourceCoordinatorInterceptor.interceptor(proceedingJoinPoint);
    }


	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}
	
	
}
