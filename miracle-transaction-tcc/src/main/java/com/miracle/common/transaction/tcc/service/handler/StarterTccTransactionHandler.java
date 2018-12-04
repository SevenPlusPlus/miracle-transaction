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
public class StarterTccTransactionHandler implements TccTransactionHandler{

	@Autowired
	private TccTransactionManager tccTransManger;
	
	@Override
	public Object handler(ProceedingJoinPoint pjp,
			TccTransactionContext tccTransactionContext) throws Throwable {
		Object retVal = null;
	
		Method method = MethodAnnotationUtil.getAnnotationedMethod(pjp);
		//根据注解获取指定的confirm方法名称和cancel方法名称
		TccTransactional tcc = method.getAnnotation(TccTransactional.class);
		
		try
		{
			final TccTransaction tccTrans = this.tccTransManger.begin(pjp);
			try{
				retVal = pjp.proceed();
				tccTrans.setStatus(TccStatus.TRY_DONE.getStatus());
				tccTransManger.updateStatus(tccTrans.getTransactionId(),
                        TccStatus.TRY_DONE.getStatus());

			}
			catch(Throwable t)
			{
				tccTrans.setStatus(TccStatus.TRY_FAIL.getStatus());
				tccTransManger.updateStatus(tccTrans.getTransactionId(),
                        TccStatus.TRY_FAIL.getStatus());
				tccTransManger.cancel(tcc.asyncCancel());
				throw t;
			}
			tccTransManger.confirm(tcc.asyncConfirm());
		}
		finally{
			tccTransManger.remove();
		}
		
		return retVal;
	}

}
