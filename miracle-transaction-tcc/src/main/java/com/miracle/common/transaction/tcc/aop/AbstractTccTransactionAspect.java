package com.miracle.common.transaction.tcc.aop;

import lombok.Setter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

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
public abstract class AbstractTccTransactionAspect {

	@Setter
	private TccTransactionInterceptor tccTransactionInterceptor;
	
	//带tccTransactional注解的类都会呗aop
	@Pointcut("@annotation(com.miracle.common.transaction.annotation.TccTransactional)")
    public void tccTransactionInterceptor() {

    }

    @Around("tccTransactionInterceptor()")
    public Object interceptCompensableMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return tccTransactionInterceptor.interceptor(proceedingJoinPoint);
    }

	 /**
     * spring Order 接口，该值的返回直接会影响springBean的加载顺序
     *
     * @return int 类型
     */
    public abstract int getOrder();
}
