package com.miracle.common.transaction.tcc.coordinator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.miracle.common.transaction.api.*;
import com.miracle.common.transaction.tcc.TccSpringBeanFactory;
import com.miracle.common.transaction.tcc.config.TccConfig;
import com.miracle.common.transaction.tcc.repository.TransactionRepository;
import com.miracle.common.transaction.threadpool.TransactionThreadFactory;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miracle.common.transaction.annotation.api.TccMode;


@Component
@Slf4j
public class CoordinatorServiceImpl implements CoordinatorService{

	@Autowired
	private TccConfig tccConfig;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	private ScheduledExecutorService scheduledExecutorService;
	
	private BlockingQueue<CoordinatorAction> QUEUE;
	
	@Override
	public synchronized void start() throws Exception {
		scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
	            TransactionThreadFactory.create("tccRollBackService", true));
		
		QUEUE = new LinkedBlockingQueue<>(tccConfig.getCoordinatorQueueMax());
        final int coordinatorThreadMax = tccConfig.getCoordinatorThreadMax();
        final ExecutorService executorService = TccTransactionThreadPool.newCustomFixedThreadPool(
        				tccConfig, "coordinatorExecutor", coordinatorThreadMax);
        log.info("Start coordinator thread pool num: {}", tccConfig.getCoordinatorThreadMax());
        for (int i = 0; i < coordinatorThreadMax; i++) {
            executorService.execute(new TccWorker());
        }
        
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				try
				{
					Long delayedTimestamp = System.currentTimeMillis() - (tccConfig.getRecoverDelayTime()*1000);
					final List<TccTransaction> tccTransactions =
							transactionRepository.listAllByDelay(delayedTimestamp);
                    if (tccTransactions != null && tccTransactions.size() > 0) {

                        for (TccTransaction tccTransaction : tccTransactions) {

                            //如果try未执行完成，那么就不进行补偿 （防止在try阶段的各种异常情况）
                            if (tccTransaction.getRole() == TccRole.PROVIDER.getRole() &&
                                    tccTransaction.getStatus() == TccStatus.TRYING.getStatus()) {
                                continue;
                            }

                            if (tccTransaction.getRetriedCount() > tccConfig.getRetryMax()) {
                                log.error("This transaction tried too many times already, no more retry：{}",
                                        tccTransaction);
                                continue;
                            }
                            
                            //CC 模式如果TRY阶段异常，不需要Cancel
                            if ((tccTransaction.getMode() == TccMode.CC.getMode())
                                    && tccTransaction.getStatus() == TccStatus.TRY_FAIL.getStatus()) {
                                continue;
                            }

                            //如果事务角色是提供者的话，并且在重试的次数范围内，是不能执行的，只能由发起者执行
                            if (tccTransaction.getRole() == TccRole.PROVIDER.getRole()
                                    && (tccTransaction.getCreateTime() + (tccConfig.getRetryMax() * tccConfig.getRecoverDelayTime() * 1000)
                                    		> System.currentTimeMillis()))
                            {
                                continue;
                            }

                            try {
                                // 先更新数据，然后执行
                                tccTransaction.setRetriedCount(tccTransaction.getRetriedCount() + 1);
                                final int rows = transactionRepository.update(tccTransaction);
                                //判断当rows>0 才执行，为了防止业务方为集群模式时候的并发
                                if (rows > 0) {
                                    //如果是以下3种状态
                                    if ((tccTransaction.getStatus() == TccStatus.TRY_FAIL.getStatus()
                                            || tccTransaction.getStatus() == TccStatus.TRY_DONE.getStatus()
                                            || tccTransaction.getStatus() == TccStatus.CANCELING.getStatus())) {
                                        cancel(tccTransaction);
                                    } else if (tccTransaction.getStatus() == TccStatus.CONFIRMING.getStatus()) {
                                        //执行confirm操作
                                        confirm(tccTransaction);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("Execute transaction compensation failed", e);
                            }
                        }
                    }
				}
				catch(Throwable t)
				{
					log.error("Unexpected error while scheduled failed compensable tcc invoke", t);
				}
			}
        	
        }, 30, tccConfig.getScheduledDelay(), TimeUnit.SECONDS);
	}

	@Override
	public Long save(TccTransaction tccTransaction) {
		 final int rows = transactionRepository.create(tccTransaction);
        if (rows > 0) {
            return tccTransaction.getTransactionId();
        }
        return null;
	}

	@Override
	public TccTransaction findByTransId(Long transactionId) {
		return transactionRepository.findById(transactionId);
	}

	@Override
	public boolean remove(Long transactionId) {
		return transactionRepository.remove(transactionId) > 0;
	}

	@Override
	public void update(TccTransaction tccTransaction) {
		transactionRepository.update(tccTransaction);
	}

	@Override
	public int updateParticipant(TccTransaction tccTransaction) {
		return transactionRepository.updateParticipant(tccTransaction);
	}

	@Override
	public int updateStatus(Long transactionId, Integer status) {
		return transactionRepository.updateStatus(transactionId, status);
	}

	@Override
	public Boolean submit(CoordinatorAction coordinatorAction) {
		try {
            QUEUE.put(coordinatorAction);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
	}

	class TccWorker implements Runnable
	{

		@Override
		public void run() {
			execute();
		}
		
		private void execute() {
            while (true) {
                try {
                    final CoordinatorAction coordinatorAction = QUEUE.take();
                    if (coordinatorAction != null) {
                        final CoordinatorActionType actionType = coordinatorAction.getActionType();
                        if (CoordinatorActionType.SAVE == actionType) {
                            save(coordinatorAction.getTccTransaction());
                        } else if (CoordinatorActionType.DELETE == actionType) {
                            remove(coordinatorAction.getTccTransaction().getTransactionId());
                        } else if (CoordinatorActionType.UPDATE == actionType) {
                            update(coordinatorAction.getTccTransaction());
                        }
                    }
                } catch (Exception e) {
                    log.error("Execute CoordinatorAction failed", e);
                }
            }

        }
	}
	
	private void cancel(TccTransaction tccTransaction) {
        final List<Participant> participants = tccTransaction.getParticipants();
        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        boolean success = true;
        if (participants != null && participants.size() > 0) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setStatus(TccStatus.CANCELING.getStatus());
                    context.setTransactionId(tccTransaction.getTransactionId());
                    TccTransactionContextLocal.getInstance().set(context);
                    executeCoordinator(participant.getCancelTccInvocation());
                } catch (Exception e) {
                    log.error("Execute coordinator cancel failed:{}", e);
                    success = false;
                    failList.add(participant);
                }
            }
            executeHandler(success, tccTransaction, failList);
        }

    }

    private void confirm(TccTransaction tccTransaction) {

        final List<Participant> participants = tccTransaction.getParticipants();

        List<Participant> failList = Lists.newArrayListWithCapacity(participants.size());
        boolean success = true;
        if (participants != null && participants.size() > 0) {
            for (Participant participant : participants) {
                try {
                    TccTransactionContext context = new TccTransactionContext();
                    context.setStatus(TccStatus.CONFIRMING.getStatus());
                    context.setTransactionId(tccTransaction.getTransactionId());
                    TccTransactionContextLocal.getInstance().set(context);
                    executeCoordinator(participant.getConfirmTccInvocation());
                } catch (Exception e) {
                    log.error("Execute coordinator confirm failed:", e);
                    success = false;
                    failList.add(participant);
                }
            }
            executeHandler(success, tccTransaction, failList);
        }

    }


    private void executeHandler(boolean success, final TccTransaction currentTransaction,
                                List<Participant> failList) {
        if (success) {
        	transactionRepository.remove(currentTransaction.getTransactionId());
        } else {
            currentTransaction.setParticipants(failList);
            transactionRepository.updateParticipant(currentTransaction);
        }
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void executeCoordinator(TccInvocation tccInvocation) throws Exception {
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
             log.debug("Execute coordinator transaction:{}", tccInvocation.getTargetClass()
                    + ":" + tccInvocation.getMethodName());
        }
    }

}
