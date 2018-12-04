package com.miracle.common.transaction.tcc.config;

import javax.annotation.PostConstruct;

import com.miracle.common.transaction.api.ThreadContextLocalEditor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Component
public class TccConfig {
	 /**
     * 模块名称
     */
	@Value("${melot.transaction.tcc.moduleName}")
    private String moduleName;


    /**
     * 提供不同的序列化对象 {@linkplain SerializeEnum}
     */
	@Value("${melot.transaction.tcc.serializer}")
    private String serializer = "protostuff";

    /**
     * 回滚队列大小
     */
	@Value("${melot.transaction.tcc.coordinatorQueueMax}")
    private int coordinatorQueueMax = 5000;
	
	/**
	 * 异步补偿核心线程数
	 */
	@Value("${melot.transaction.tcc.asyncCompensateThreadCore}")
	private int asyncCompensateThreadCore = 2;
	
	/**
	 * 异步补偿最大线程数
	 */
	@Value("${melot.transaction.tcc.asyncCompensateThreadMax}")
	private int asyncCompensateThreadMax = 4;
	
    /**
     * 监听回滚队列线程数
     */
	@Value("${melot.transaction.tcc.coordinatorThreadMax}")
    private int coordinatorThreadMax = Runtime.getRuntime().availableProcessors() << 1;


    /**
     * 任务调度线程大小
     */
	@Value("${melot.transaction.tcc.scheduledThreadMax}")
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 调度时间周期 单位秒
     */
	@Value("${melot.transaction.tcc.scheduledDelay}")
    private int scheduledDelay = 60;

    /**
     * 最大重试次数
     */
	@Value("${melot.transaction.tcc.retryMax}")
    private int retryMax = 3;


    /**
     * 事务恢复间隔时间 单位秒（注意 此时间表示本地事务创建的时间多少秒以后才会执行）
     */
	@Value("${melot.transaction.tcc.recoverDelayTime}")
    private int recoverDelayTime = 60;


    /**
     * 线程池的拒绝策略 {@linkplain com.happylifeplat.tcc.common.enums.RejectedPolicyTypeEnum}
     */
	@Value("${melot.transaction.tcc.rejectPolicy}")
    private String rejectPolicy = "Abort";

    /**
     * 线程池的队列类型 {@linkplain com.happylifeplat.tcc.common.enums.BlockingQueueTypeEnum}
     */
	@Value("${melot.transaction.tcc.blockingQueueType}")
    private String blockingQueueType = "Linked";


    /**
     * 补偿存储类型 {@linkplain com.happylifeplat.tcc.common.enums.RepositorySupportEnum}
     */
	@Value("${melot.transaction.tcc.repositoryType}")
    private String repositoryType = "db";
	
	@Value("${melot.transaction.tcc.dbType}")
    private String dbType = "mysql";
	
	@Value("${melot.transaction.tcc.datasourceName}")
	private String datasourceName = "tcc";
	
	/**
	 * rpc等传递的线程上下文编辑处理类
	 */
	@Value("${melot.transaction.tcc.threadContextLocalEditorClass}")
	private String threadContextLocalEditorClass = "com.melot.common.transaction.api.ThreadContextLocalEditor";
	
	private ThreadContextLocalEditor threadContextLocalEditor;
	
	@PostConstruct
	private void init()
	{
		try {
			Class<?> cls = Class.forName(threadContextLocalEditorClass);
			Object editor = cls.newInstance();
			if(editor instanceof ThreadContextLocalEditor)
			{
				threadContextLocalEditor = (ThreadContextLocalEditor) editor;
			}
			else
			{
				log.error("Bad class tccConfig for threadContextLocalEditorClass {}", threadContextLocalEditorClass);
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			log.error("Class not found {}", threadContextLocalEditorClass, e);
		}
	}

}
