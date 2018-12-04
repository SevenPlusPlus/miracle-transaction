package com.miracle.common.transaction.tcc.coordinator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.miracle.common.transaction.tcc.config.TccConfig;
import com.miracle.common.transaction.threadpool.BlockingQueueType;
import com.miracle.common.transaction.threadpool.RejectedPolicyType;
import com.miracle.common.transaction.threadpool.TransactionThreadFactory;
import com.miracle.common.transaction.threadpool.policy.*;

public class TccTransactionThreadPool {

    private static final int MAX_ARRAY_QUEUE = 1000;


    private static RejectedExecutionHandler createPolicy(TccConfig tccConfig) {
        RejectedPolicyType rejectedPolicyType = RejectedPolicyType.fromString(tccConfig.getRejectPolicy());
        switch (rejectedPolicyType) {
            case BLOCKING_POLICY:
                return new BlockingPolicy();
            case CALLER_RUNS_POLICY:
                return new CallerRunsPolicy();
            case ABORT_POLICY:
                return new AbortPolicy();
            case REJECTED_POLICY:
                return new RejectedPolicy();
            case DISCARDED_POLICY:
                return new DiscardedPolicy();
            default:
                return new AbortPolicy();
        }
    }

    private static BlockingQueue<Runnable> createBlockingQueue(TccConfig tccConfig) {
        BlockingQueueType queueType = BlockingQueueType.fromString(tccConfig.getBlockingQueueType());

        switch (queueType) {
            case LINKED_BLOCKING_QUEUE:
                return new LinkedBlockingQueue<>();
            case ARRAY_BLOCKING_QUEUE:
                return new ArrayBlockingQueue<>(MAX_ARRAY_QUEUE);
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<>();
            default:
                return new LinkedBlockingQueue<>();
        }

    }

    public static ExecutorService newCustomFixedThreadPool(TccConfig tccConfig, String name, int threads) {
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                createBlockingQueue(tccConfig),
                TransactionThreadFactory.create(name, false), createPolicy(tccConfig));
    }

    public static ExecutorService newCustomCacheableThreadPool(TccConfig tccConfig, String name, int coreThreads, int maxThreads)
    {
    	 return new ThreadPoolExecutor(coreThreads, maxThreads, 0, TimeUnit.MILLISECONDS,
                 createBlockingQueue(tccConfig),
                 TransactionThreadFactory.create(name, false), createPolicy(tccConfig));
    }
}

