package com.miracle.common.transaction.threadpool.policy;

public interface RejectedRunnable extends Runnable {

    /**
     * 线程池拒绝操作
     */
    void rejected();
}
