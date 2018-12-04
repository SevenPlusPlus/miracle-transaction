package com.miracle.common.transaction.log.aop;

import lombok.Setter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

public abstract class AbstractLogTransactionAspect {

	@Setter
	private LogTransactionInterceptor logTransactionInterceptor;

	@Pointcut("@annotation(com.miracle.common.transaction.annotation.LogTransactional) || @within(com.miracle.common.transaction.annotation.ClassTransactional) ")
	public void logTransactionInterceptor() {

	}
	
	
	@Pointcut("this(LogAopMaker)")
	public void aopMaker() {
		
	}
	
	@Around("logTransactionInterceptor() || aopMaker()")
	public Object interceptCompensableMethod(
			ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		return logTransactionInterceptor.interceptor(proceedingJoinPoint);
	}

	/**
	 * spring Order 接口，该值的返回直接会影响springBean的加载顺序
	 * 
	 * @return int 类型
	 */
	public abstract int getOrder();
}
