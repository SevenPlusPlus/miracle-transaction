package com.miracle.common.transaction.log;

import java.util.Map;

import com.miracle.common.miracle_utils.StringUtils;
import com.miracle.common.mq.MiracleHistory;
import com.miracle.common.mq.history.EventHistory;
import com.miracle.common.mq.history.History;
import com.miracle.common.mq.history.TransactionHistory;
import com.miracle.common.transaction.api.LogTransactionContext;
import com.miracle.common.transaction.api.LogTransactionContextLocal;
import com.miracle.common.transaction.api.TransactionLog;
import com.miracle.common.transaction.util.LogUtils;
import lombok.Getter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class LogTransactionService {
	
	@Autowired(required=false)
	@Getter
	private MiracleHistory miracleHistory;

	public TransactionLog createTransactionLog(ProceedingJoinPoint pjp, Map<String, String> attachments)
			throws Throwable {
		String className = pjp.getTarget().getClass().getSimpleName();
		String methodName = pjp.getSignature().getName();
		Map<String, Object> paramMap = LogUtils.createMethodParameterMap(pjp);
		TransactionLog transactionLog = new TransactionLog(className, methodName, paramMap, attachments);
	
		return transactionLog;
	}

	
	public Object processWithTransactionLog(ProceedingJoinPoint pjp,
			 String transactionId, String module, TransactionLog transactionLog)
			throws Throwable {
		Object retObj = null;
		TransactionHistory trans = null;
		try {
			String type = module;
			if(StringUtils.isEmpty(module))
			{
				type = transactionLog.getClassName();
			}
			type = type + "-" + transactionLog.getMethodName();
			trans = miracleHistory.newTransactionHistory(type, transactionId);
			LogTransactionContext context = LogTransactionContextLocal.getInstance().get();
			if (context == null) {
				context = new LogTransactionContext();
				context.setTransOrderId(transactionId);
				LogTransactionContextLocal.getInstance().set(context);
			}
			trans.addAttribute("LogTransaction", transactionLog);
			retObj = pjp.proceed();
			trans.setStatus(History.SUCCESS);
		}
		catch(Throwable t)
		{
			String failedStatus = t.getClass().getSimpleName();
			if(t.getMessage() != null)
			{
				failedStatus = failedStatus + ":" + t.getMessage();
			}
			trans.setStatus(failedStatus);
			throw t;
		}
		finally {
			if(trans != null)
			{
				trans.complete();
				LogTransactionContextLocal.getInstance().remove();
			}
		}
		return retObj;
	}
	
	public Object processWithEventLog(ProceedingJoinPoint pjp,
			String module, TransactionLog transactionLog) throws Throwable {
		Object retObj = null;
		EventHistory event = null;
		try
		{
			String type = module;
			if(StringUtils.isEmpty(module))
			{
				type = transactionLog.getClassName();
			}
			type = type + "-" + transactionLog.getMethodName();
			String eventId = LogTransactionContextLocal.getInstance().get().getTransEventSeqId();
			String newEventId = LogUtils.getNextEventId(eventId);
			event = miracleHistory.newEventHistory(type, newEventId);
			LogTransactionContextLocal.getInstance().get().setTransEventSeqId(newEventId);
			event.addAttribute("LogTransaction", transactionLog);
			retObj = pjp.proceed();
			event.setStatus(History.SUCCESS);
		}
		catch(Throwable t)
		{
			String failedStatus = t.getClass().getSimpleName();
			if(t.getMessage() != null)
			{
				failedStatus = failedStatus + ":" + t.getMessage();
			}
			event.setStatus(failedStatus);
			throw t;
		}
		finally
		{
			if(event != null)
			{
				event.complete();
			}
		}
		return retObj;
	}
}
