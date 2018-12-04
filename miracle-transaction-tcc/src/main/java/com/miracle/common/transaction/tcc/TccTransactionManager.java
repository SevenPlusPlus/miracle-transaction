package com.miracle.common.transaction.tcc;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.miracle.common.transaction.api.*;
import com.miracle.common.transaction.exception.TccRuntimeException;
import com.miracle.common.transaction.tcc.config.TccConfig;
import com.miracle.common.transaction.tcc.coordinator.CoordinatorAction;
import com.miracle.common.transaction.tcc.coordinator.CoordinatorActionType;
import com.miracle.common.transaction.tcc.coordinator.CoordinatorExecutor;
import com.miracle.common.transaction.tcc.coordinator.CoordinatorService;
import com.miracle.common.transaction.utils.MethodAnnotationUtil;
import com.miracle.common.transaction.utils.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miracle.common.transaction.annotation.api.TccMode;


@Component
@Slf4j
public class TccTransactionManager {
	
	/**
     * 将事务信息存放在threadLocal里面
     */
    private static final ThreadLocal<TccTransaction> CURRENT = new ThreadLocal<>();
    
    @Autowired
    private TccConfig tccConfig;
    
    @Autowired
    private CoordinatorService coordinatorService;
    
    @Autowired
    private CoordinatorExecutor coordinatorExecutor;
    
    @Autowired
    private TccAsyncCompensateService asyncCompensateService;
    
	@Autowired
	private SnowflakeIdGenerator snowflakeIdGenerator;
    /**
     * 该方法为发起方第一次调用
     * 也是tcc事务的入口
     */
    public TccTransaction begin(ProceedingJoinPoint point) {
        log.debug("Start tcc transaction! start");
        TccTransaction tccTransaction = CURRENT.get();
        if (tccTransaction == null) {
            Method method = MethodAnnotationUtil.getAnnotationedMethod(point);

            Class<?> clazz = point.getTarget().getClass();

            tccTransaction = new TccTransaction(snowflakeIdGenerator.nextId());
            tccTransaction.setStatus(TccStatus.TRYING.getStatus());
            tccTransaction.setRole(TccRole.STARTER.getRole());
            tccTransaction.setTargetClass(clazz.getName());
            tccTransaction.setTargetMethod(method.getName());
        }
        //保存当前事务信息
        coordinatorExecutor.execute(new CoordinatorAction(CoordinatorActionType.SAVE, tccTransaction));

        CURRENT.set(tccTransaction);

        //设置tcc事务上下文，这个类会传递给远端
        TccTransactionContext context = new TccTransactionContext();
        //设置执行动作为try
        context.setStatus(TccStatus.TRYING.getStatus());
        //设置事务id
        context.setTransactionId(tccTransaction.getTransactionId());
        TccTransactionContextLocal.getInstance().set(context);

        return tccTransaction;

    }

    public TccTransaction providerBegin(TccTransactionContext context, ProceedingJoinPoint point) {
        log.debug("Provider start tcc transaction! start{}", context.toString());

        Method method = MethodAnnotationUtil.getAnnotationedMethod(point);

        Class<?> clazz = point.getTarget().getClass();
        TccTransaction transaction = new TccTransaction(context.getTransactionId());
        //设置角色为提供者
        transaction.setRole(TccRole.PROVIDER.getRole());
        transaction.setStatus(TccStatus.TRYING.getStatus());

        transaction.setTargetClass(clazz.getName());
        transaction.setTargetMethod(method.getName());
        //保存当前事务信息
        coordinatorService.save(transaction);
        //传入当前threadLocal
        CURRENT.set(transaction);
        return transaction;
    }

    public TccTransaction acquire(TccTransactionContext context) {
        final TccTransaction tccTransaction = coordinatorService.findByTransId(context.getTransactionId());
        CURRENT.set(tccTransaction);
        return tccTransaction;
    }


    private void cancelTransaction(TccTransaction currentTransaction)
    {
        //获取回滚节点
        final List<Participant> participants = currentTransaction.getParticipants();

        currentTransaction.setStatus(TccStatus.CANCELING.getStatus());

        //先异步更新数据
        coordinatorExecutor.execute(new CoordinatorAction(CoordinatorActionType.UPDATE, currentTransaction));
        boolean success = true;
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        if (participants != null && participants.size() > 0) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setStatus(TccStatus.CANCELING.getStatus());
                    context.setTransactionId(participant.getTransactionId());
                    TccTransactionContextLocal.getInstance().set(context);
                    executeParticipantMethod(participant.getCancelTccInvocation());

                } catch (Exception e) {
                    log.error("执行cancel方法异常:", e);
                    success = false;
                    failList.add(participant);
                }
            }

            executeHandler(success, currentTransaction, failList);
        }
    }
    
    /**
     * 调用回滚接口
     */
    public void cancel(boolean asyncCancel) {
        log.debug("Start tcc cancel! start");

        final TccTransaction currentTransaction = getCurrentTransaction();
        if (currentTransaction == null) {
            return;
        }
        
        if((currentTransaction.getRole() == TccRole.PROVIDER.getRole()) && 
        		(currentTransaction.getStatus() == TccStatus.TRYING.getStatus()))
        {
        	//此时provider transaction try阶段尚未完成，只能先return，后续Try执行完成后由CoordinateService异步发起Cancel
        	return;
        }
        
        //如果是cc模式，那么在try阶段异常是不会进行cancel补偿
        if (currentTransaction.getStatus() == TccStatus.TRY_FAIL.getStatus()  &&
                currentTransaction.getMode() == TccMode.CC.getMode()) 
        {
        	coordinatorExecutor.execute(new CoordinatorAction(CoordinatorActionType.DELETE, currentTransaction));
            return;
        }
        
        if(asyncCancel)
        {
        	try{
	        	asyncCompensateService.execute(new Runnable(){
	
					@Override
					public void run() {
						cancelTransaction(currentTransaction);
					}
	        		
	        	});
        	}
        	catch(Throwable t)
        	{
        		log.error("Async execute transaction cancel task failed.", t);
        	}
        }
        else
        {
        	cancelTransaction(currentTransaction);
        }
    }


    private void confirmTransaction(TccTransaction currentTransaction)
    {
         final List<Participant> participants = currentTransaction.getParticipants();

         List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
         boolean success = true;
         if (participants != null && participants.size() > 0) {
             for (Participant participant : participants) {
                 try {
                     TccTransactionContext context = new TccTransactionContext();
                     context.setStatus(TccStatus.CONFIRMING.getStatus());
                     context.setTransactionId(participant.getTransactionId());
                     TccTransactionContextLocal.getInstance().set(context);
                     executeParticipantMethod(participant.getConfirmTccInvocation());
                 } catch (Exception e) {
                     log.error("Execute confirm failed:", e);
                     success = false;
                     failList.add(participant);
                 }
             }
             executeHandler(success, currentTransaction, failList);
         }
    }
    
    /**
     * 调用confirm方法 这里主要如果是发起者调用 这里调用远端的还是原来的方法，不过上下文设置了调用confirm
     * 那么远端的服务则会调用confirm方法。。
     */
    public void confirm(boolean asyncConfirm) throws TccRuntimeException {

        log.debug("Start tcc confirm! start");

        final TccTransaction currentTransaction = getCurrentTransaction();

        if (null == currentTransaction) {
            return;
        }
        
        //此处不需要进行此判断，因为如果provider超时时，starter不可能发起commit操作
        //if((currentTransaction.getRole() == TccRole.PROVIDER.getRole()) && 
		//(currentTransaction.getStatus() == TccStatus.TRYING.getStatus()))
        
        currentTransaction.setStatus(TccStatus.CONFIRMING.getStatus());
        coordinatorExecutor.execute(new CoordinatorAction(CoordinatorActionType.UPDATE, currentTransaction));
        
        if(asyncConfirm)
        {
        	try{
	        	asyncCompensateService.execute(new Runnable(){
	
					@Override
					public void run() {
						confirmTransaction(currentTransaction);
					}
	        		
	        	});
        	}
        	catch(Throwable t)
        	{
        		log.error("Async execute transaction commit task failed.", t);
        	}
        }
        else
        {
        	confirmTransaction(currentTransaction);
        }
    }


    private void executeHandler(boolean success, final TccTransaction currentTransaction,
                                List<Participant> failList) {
        if (success) {
            TccTransactionContextLocal.getInstance().remove();
            coordinatorExecutor.execute(new CoordinatorAction(CoordinatorActionType.DELETE, currentTransaction));
        } else {
            //获取还没执行的，或者执行失败的
            currentTransaction.setParticipants(failList);
            coordinatorService.updateParticipant(currentTransaction);
            throw new TccRuntimeException(failList.toString());
        }
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void executeParticipantMethod(TccInvocation tccInvocation) throws Exception {
        if (tccInvocation != null) {
        	Map<String, String> attachments = tccInvocation.getAttachments();
        	ThreadContextLocalEditor editor = tccConfig.getThreadContextLocalEditor();
        	editor.setLocalContextFromAttachments(attachments);
        	
            final Class clazz = tccInvocation.getTargetClass();
            final String method = tccInvocation.getMethodName();
            final Object[] args = tccInvocation.getArgs();
            final Class[] parameterTypes = tccInvocation.getParameterTypes();
            final Object bean = TccSpringBeanFactory.getInstance().getBean(clazz);
            
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);

        }
    }

    public boolean isBegin() {
        return CURRENT.get() != null;
    }


    public void remove() {
        CURRENT.remove();
    }


    public TccTransaction getCurrentTransaction() {
        return CURRENT.get();
    }


   public void removeTccTransaction(TccTransaction tccTransaction) {
        coordinatorService.remove(tccTransaction.getTransactionId());
    }


    public void updateStatus(Long transId, Integer status) {
        coordinatorService.updateStatus(transId, status);
    }

    public void enlistParticipant(Participant participant) {
        final TccTransaction transaction = this.getCurrentTransaction();
        transaction.registerParticipant(participant);
        coordinatorService.update(transaction);

    }
}
