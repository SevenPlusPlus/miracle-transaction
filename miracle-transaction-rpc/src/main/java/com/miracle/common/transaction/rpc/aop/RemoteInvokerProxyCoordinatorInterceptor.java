package com.miracle.common.transaction.rpc.aop;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

import com.miracle.common.transaction.api.*;
import com.miracle.common.transaction.exception.TccRuntimeException;
import com.miracle.common.transaction.tcc.TccTransactionManager;
import com.miracle.common.transaction.tcc.config.TccConfig;
import com.miracle.common.transaction.utils.MethodAnnotationUtil;
import com.miracle.module.rpc.core.api.coordinator.InterceptorInvoker;
import com.miracle.module.rpc.core.api.coordinator.MethodProceedingJoinPoint;
import com.miracle.module.rpc.core.api.coordinator.ProxyCoordinatorInterceptor;
import com.miracle.module.rpc.core.api.coordinator.ProxyCoordinatorInterceptorManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miracle.common.transaction.annotation.TccTransactional;
import com.miracle.common.transaction.annotation.api.TccMode;



@Component
public class RemoteInvokerProxyCoordinatorInterceptor implements ProxyCoordinatorInterceptor {

	@Autowired
	private TccTransactionManager tccTransactionManager;
	
	@Autowired
	private TccConfig tccConfig;
	
	@PostConstruct
	private void init()
	{
		ProxyCoordinatorInterceptorManager.getInstance().registerProxyCoordinatorInterceptor(this);
	}
	
	@Override
	public Object interceptProxyCoordinatorMethod(InterceptorInvoker invoker, ProceedingJoinPoint pjp) throws Throwable {
	
		if(pjp instanceof MethodProceedingJoinPoint)
		{
			MethodProceedingJoinPoint mpjp = (MethodProceedingJoinPoint)pjp;
			Method method = MethodAnnotationUtil.getAnnotationedMethod(mpjp);
			
			TccTransactional tcc = method.getAnnotation(TccTransactional.class);
			if(tcc != null)
			{
				Participant participant = buildParticipant(tcc, method, mpjp.getInterfaceClass(),
						mpjp.getArgs(), method.getParameterTypes());
				if(participant != null)
				{
					tccTransactionManager.enlistParticipant(participant);
				}
			}
		}
		
		Object retObj = invoker.invoke(pjp);
		
		return retObj;
	}
	
	@SuppressWarnings({ "rawtypes" })
    private Participant buildParticipant(TccTransactional tcc, Method method,
    		Class clazz, Object[] arguments, Class... args) throws TccRuntimeException {

		TccTransactionContext tccTransactionContext = TccTransactionContextLocal.getInstance().get();
		
        if (tccTransactionContext != null) {
            if (TccStatus.TRYING.getStatus() == tccTransactionContext.getStatus()) {
                //获取协调方法, 对于rpc调用方来说协调方法只能是接口发起调用的方法
                String confirmMethodName = method.getName();
                String cancelMethodName = method.getName();

                //设置模式
                final TccMode mode = tcc.mode();

                tccTransactionManager.getCurrentTransaction().setMode(mode.getMode());

                ThreadContextLocalEditor editor = tccConfig.getThreadContextLocalEditor();
                
                TccInvocation confirmInvocation = new TccInvocation(clazz,
                        confirmMethodName, args, arguments,
                        editor.getLocalContextAttachments());

                TccInvocation cancelInvocation = new TccInvocation(clazz,
                        cancelMethodName, args, arguments,
                        editor.getLocalContextAttachments());
                //封装调用点
                return new Participant(
                        tccTransactionContext.getTransactionId(),
                        confirmInvocation,
                        cancelInvocation);
            }

        }
        return null;
    }

}
