package com.miracle.common.transaction.tcc.aop;

import org.aspectj.lang.ProceedingJoinPoint;

public interface TccTransactionInterceptor {
	/**
     * tcc分布式事务拦截方法
     *
     * @param pjp tcc切入点
     * @return Object
     * @throws Throwable 异常
     */
    Object interceptor(ProceedingJoinPoint pjp) throws Throwable;
}
