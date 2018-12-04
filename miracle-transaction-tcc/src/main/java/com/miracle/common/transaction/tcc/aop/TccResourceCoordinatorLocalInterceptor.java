package com.miracle.common.transaction.tcc.aop;

import java.lang.reflect.Method;

import com.miracle.common.transaction.api.*;
import com.miracle.common.transaction.tcc.TccTransactionManager;
import com.miracle.common.transaction.tcc.config.TccConfig;
import com.miracle.common.transaction.utils.MethodAnnotationUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle.common.transaction.annotation.TccTransactional;
import com.miracle.common.transaction.annotation.api.TccMode;

@Component
public class TccResourceCoordinatorLocalInterceptor {

	@Autowired
	private TccTransactionManager tccTransManager;
	
	@Autowired
	private TccConfig tccConfig;
	 
	public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
		
		final TccTransaction currentTransaction = tccTransManager.getCurrentTransaction();
		if(currentTransaction != null)
		{
			final TccStatus status = TccStatus.valueOf(currentTransaction.getStatus());
			switch(status)
			{
			case TRYING:
				registerParticipant(pjp, currentTransaction.getTransactionId());
				break;
			case TRY_DONE:
				break;
			case CONFIRMING:
                break;
            case CANCELING:
                break;
            default:
                break;
			}
		}
		return pjp.proceed();
	}
	
	 /**
     * 获取调用接口的协调方法并封装
     *
     * @param point 切点
     */
    private void registerParticipant(ProceedingJoinPoint point, Long transId) throws NoSuchMethodException {

        Method method = MethodAnnotationUtil.getAnnotationedMethod(point);

        Class<?> clazz = point.getTarget().getClass();

        Object[] args = point.getArgs();

        final TccTransactional tcc = method.getAnnotation(TccTransactional.class);

        //获取协调方法
        String confirmMethodName = tcc.confirmMethod();

        String cancelMethodName = tcc.cancelMethod();

        //设置模式
        final TccMode mode = tcc.mode();

        tccTransManager.getCurrentTransaction().setMode(mode.getMode());

        ThreadContextLocalEditor editor = tccConfig.getThreadContextLocalEditor();
        
        TccInvocation confirmInvocation = null;
        if (StringUtils.isNoneBlank(confirmMethodName)) {
            confirmInvocation = new TccInvocation(clazz, confirmMethodName, method.getParameterTypes(),
                     args, editor.getLocalContextAttachments());
        }

        TccInvocation cancelInvocation = null;
        if (StringUtils.isNoneBlank(cancelMethodName)) {
            cancelInvocation = new TccInvocation(clazz, cancelMethodName,
                    method.getParameterTypes(), args, editor.getLocalContextAttachments());
        }

        if(confirmInvocation != null || cancelInvocation != null)
        {
	        //封装调用点
	        final Participant participant = new Participant(
	                transId,
	                confirmInvocation,
	                cancelInvocation);
	
	        tccTransManager.enlistParticipant(participant);
        }
    }
}
