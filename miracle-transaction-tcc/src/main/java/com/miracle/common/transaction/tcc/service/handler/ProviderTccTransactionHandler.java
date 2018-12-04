package com.miracle.common.transaction.tcc.service.handler;

import java.lang.reflect.Method;

import com.miracle.common.transaction.api.TccStatus;
import com.miracle.common.transaction.api.TccTransaction;
import com.miracle.common.transaction.api.TccTransactionContext;
import com.miracle.common.transaction.tcc.TccTransactionManager;
import com.miracle.common.transaction.utils.MethodAnnotationUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle.common.transaction.annotation.TccTransactional;


@Component
public class ProviderTccTransactionHandler implements TccTransactionHandler{

	@Autowired
	private TccTransactionManager tccTransManager;
	
	/**
     * 分布式事务提供者处理接口
     * 根据tcc事务上下文的状态来执行相对应的方法
     *
     * @param point   point 切点
     * @param context context
     * @return Object
     * @throws Throwable 异常
     */
	@Override
	public Object handler(ProceedingJoinPoint pjp,
			TccTransactionContext tccTransactionContext) throws Throwable {
		TccTransaction tccTransaction = null;
		
		Method method = MethodAnnotationUtil.getAnnotationedMethod(pjp);
		TccTransactional tcc = method.getAnnotation(TccTransactional.class);
		
        try {
            switch (TccStatus.valueOf(tccTransactionContext.getStatus())) {
                case TRYING:
                	//After try phase, mush be confirmed or cancelled later to clear transaction record
                    //创建事务信息
                    tccTransaction = tccTransManager.providerBegin(tccTransactionContext, pjp);
                    //发起方法调用
                    Object proceed = null;
                    try{
	                    proceed = pjp.proceed();
	                    tccTransaction.setStatus(TccStatus.TRY_DONE.getStatus());
	                    tccTransManager.updateStatus(tccTransaction.getTransactionId(),
	                            TccStatus.TRY_DONE.getStatus());
                    }
                    catch(Throwable t)
                    {
                    	tccTransaction.setStatus(TccStatus.TRY_FAIL.getStatus());
 	                    tccTransManager.updateStatus(tccTransaction.getTransactionId(),
 	                            TccStatus.TRY_FAIL.getStatus());
                    	throw t;
                    }
                    return proceed;
                case CONFIRMING:
                    //如果是confirm 通过之前保存的事务信息 进行反射调用
                	tccTransManager.acquire(tccTransactionContext);
                	tccTransManager.confirm(tcc.asyncConfirm());
                    break;
                case CANCELING:
                    //如果是调用CANCELING 通过之前保存的事务信息 进行反射调用
                	tccTransManager.acquire(tccTransactionContext);
                	tccTransManager.cancel(tcc.asyncCancel());
                    break;
                default:
                    break;
            }
        } finally {
        	tccTransManager.remove();
        }
        return getDefaultValue(method.getReturnType());
	}
	
	@SuppressWarnings("rawtypes")
	private Object getDefaultValue(Class type) {

        if (boolean.class.equals(type)) {
            return false;
        } else if (byte.class.equals(type)) {
            return 0;
        } else if (short.class.equals(type)) {
            return 0;
        } else if (int.class.equals(type)) {
            return 0;
        } else if (long.class.equals(type)) {
            return 0;
        } else if (float.class.equals(type)) {
            return 0;
        } else if (double.class.equals(type)) {
            return 0;
        }

        return null;
    }

}
