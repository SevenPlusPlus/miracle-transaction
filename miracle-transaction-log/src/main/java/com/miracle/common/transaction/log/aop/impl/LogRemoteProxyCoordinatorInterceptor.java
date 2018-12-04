package com.miracle.common.transaction.log.aop.impl;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.miracle.common.transaction.api.LogTransactionContext;
import com.miracle.common.transaction.api.LogTransactionContextLocal;
import com.miracle.common.transaction.api.ThreadContextLocalEditor;
import com.miracle.common.transaction.api.TransactionLog;
import com.miracle.common.transaction.log.LogTransactionService;
import com.miracle.common.transaction.utils.MethodAnnotationUtil;
import com.miracle.module.rpc.core.api.coordinator.InterceptorInvoker;
import com.miracle.module.rpc.core.api.coordinator.ProxyCoordinatorInterceptor;
import com.miracle.module.rpc.core.api.coordinator.ProxyCoordinatorInterceptorManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle.common.transaction.annotation.api.Propagation;


@Component
public class LogRemoteProxyCoordinatorInterceptor implements
		ProxyCoordinatorInterceptor {

	@Autowired
	private LogTransactionService logTransactionService;

	private ThreadContextLocalEditor threadContextLocalEditor = new ThreadContextLocalEditor();

	@Autowired
	private LogTransactionHandler logTransactionHandler;
	
	@PostConstruct
	private void init() {
		ProxyCoordinatorInterceptorManager.getInstance()
				.registerProxyCoordinatorInterceptor(this);
	}

	@Override
	public Object interceptProxyCoordinatorMethod(InterceptorInvoker invoker, ProceedingJoinPoint pjp)
			throws Throwable {
		if (logTransactionService.getMiracleHistory() == null) {
			return invoker.invoke(pjp);
		}

		Object retObj = null;
		Method method = MethodAnnotationUtil.getAnnotationedMethod(pjp);

		LogTransactionHandler.TransactionProperties transProps = logTransactionHandler.handle(method);

		if (transProps == null) {
			return invoker.invoke(pjp);
		}

		Propagation propagation = transProps.getPropagation();
		if (propagation.equals(Propagation.PROPAGATION_SUPPORTS)
				|| propagation.equals(Propagation.PROPAGATION_REQUIRED)) {
			final LogTransactionContext logTransactionContext = LogTransactionContextLocal
					.getInstance().get();
			if (logTransactionContext != null
					&& logTransactionService.getMiracleHistory().isStartTrans()) {
				Map<String, String> attachments = threadContextLocalEditor
						.getLocalContextAttachments();
				TransactionLog transactionLog = logTransactionService
						.createTransactionLog(pjp, attachments);
				retObj = logTransactionService.processWithEventLog(pjp,
						transProps.getModule(), transactionLog);
			} else {
				retObj = invoker.invoke(pjp);
			}
		} else {
			retObj = invoker.invoke(pjp);
		}
		return retObj;
	}

}
