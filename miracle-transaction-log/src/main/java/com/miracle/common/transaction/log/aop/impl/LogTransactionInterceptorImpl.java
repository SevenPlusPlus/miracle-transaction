package com.miracle.common.transaction.log.aop.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.miracle.common.transaction.api.LogTransactionContext;
import com.miracle.common.transaction.api.LogTransactionContextLocal;
import com.miracle.common.transaction.api.ThreadContextLocalEditor;
import com.miracle.common.transaction.api.TransactionLog;
import com.miracle.common.transaction.log.LogTransactionService;
import com.miracle.common.transaction.log.aop.LogTransactionInterceptor;
import com.miracle.common.transaction.utils.MethodAnnotationUtil;
import com.miracle.common.transaction.utils.SnowflakeIdGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle.common.transaction.annotation.api.Propagation;


@Component
public class LogTransactionInterceptorImpl implements LogTransactionInterceptor {
	
	@Autowired
	private LogTransactionService logTransactionService;
	
	@Autowired
	private LogTransactionHandler logTransactionHandler;
	
	private ThreadContextLocalEditor threadContextLocalEditor = new ThreadContextLocalEditor();

	@Autowired
	private SnowflakeIdGenerator snowflakeIdGenerator;
	
	@Override
	public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
		
		if (logTransactionService.getMiracleHistory() == null) {
			return pjp.proceed();
		}

		Object retObj = null;
		Method method = MethodAnnotationUtil.getAnnotationedMethod(pjp);
		
		LogTransactionHandler.TransactionProperties transProps= logTransactionHandler.handle(method);
		
		if (transProps == null) {
			return pjp.proceed();
		}

		/*
		 * 事务传播属性优先级  classTransaction < transactionAttributes < logTransaction
		 * required沿用事务状态, 当前没有事务会创建事务, supports表示不改变,
		 * 根据事务传播属性和当前线程变量中的事务状态去判定添加action或者event或者不操作。
		 * 
		 * 当前有transId,但是没有start trans,说明这是一次远端调用传递.需要根据传递的tid创建事务。
		 */
		Propagation propagation = transProps.getPropagation();
		final LogTransactionContext logTransactionContext =
				 LogTransactionContextLocal.getInstance().get();
		Map<String, String> threadLocalAttachments = threadContextLocalEditor.getLocalContextAttachments();
		TransactionLog transactionLog = logTransactionService.createTransactionLog(pjp, threadLocalAttachments);
		if (propagation.equals(Propagation.PROPAGATION_REQUIRED))
		{
			if (logTransactionContext == null) {
				if (!logTransactionService.getMiracleHistory().isStartTrans()) {
					// 新建流水
					String transactionId = String.valueOf(snowflakeIdGenerator.nextId());
					retObj = logTransactionService.processWithTransactionLog(pjp, transactionId, 
							transProps.getModule(), transactionLog);
				} else {
					retObj = pjp.proceed();
				}
			} else {
				if (!logTransactionService.getMiracleHistory().isStartTrans()) {
					// 新建流水
					String transactionId = logTransactionContext.getTransOrderId();
					retObj = logTransactionService.processWithTransactionLog(pjp, transactionId, 
							transProps.getModule(), transactionLog);
				} else {
					retObj = logTransactionService.processWithEventLog(pjp, transProps.getModule(),
							transactionLog);
				}
			}
		} else if (propagation.equals(Propagation.PROPAGATION_SUPPORTS)) {
			if (logTransactionContext != null && logTransactionService.getMiracleHistory().isStartTrans()) {
				retObj = logTransactionService.processWithEventLog(pjp, transProps.getModule(),
						transactionLog);
			} else {
				retObj = pjp.proceed();
			}
		} else {
			retObj = pjp.proceed();
		}
		return retObj;
	}
	
}
