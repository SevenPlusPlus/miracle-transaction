package com.miracle.common.transaction.tcc.service.handler;

import com.miracle.common.transaction.api.TccTransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;


public interface TccTransactionHandler {
	/**
     * 分布式事务处理接口
     *
     * @param pjp                 point 切点
     * @param tccTransactionContext tcc事务上下文
     * @return Object
     * @throws Throwable 异常
     */
    Object handler(ProceedingJoinPoint pjp, TccTransactionContext tccTransactionContext) throws Throwable;
}
