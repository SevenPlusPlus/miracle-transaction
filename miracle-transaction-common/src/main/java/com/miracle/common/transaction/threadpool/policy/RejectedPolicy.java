package com.miracle.common.transaction.threadpool.policy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RejectedPolicy implements RejectedExecutionHandler {

    private final String threadName;

    public RejectedPolicy() {
        this(null);
    }

    public RejectedPolicy(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        if (threadName != null) {
            log.error("tccTransaction Thread pool [{}] is exhausted, executor={}", threadName, executor.toString());
        }

        if (runnable instanceof RejectedRunnable) {
            ((RejectedRunnable) runnable).rejected();
        } else {
            if (!executor.isShutdown()) {
                BlockingQueue<Runnable> queue = executor.getQueue();
                int discardSize = queue.size() >> 1;
                for (int i = 0; i < discardSize; i++) {
                    queue.poll();
                }

                try {
                    queue.put(runnable);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

