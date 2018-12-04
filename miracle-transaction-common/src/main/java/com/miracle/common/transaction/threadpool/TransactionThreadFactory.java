package com.miracle.common.transaction.threadpool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionThreadFactory implements ThreadFactory {

    private final AtomicLong threadNumber = new AtomicLong(1);

    private final String namePrefix;

    private static volatile boolean daemon;

    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("tccTransaction");

    public static ThreadGroup getThreadGroup() {
        return THREAD_GROUP;
    }

    public static ThreadFactory create(String namePrefix, boolean daemon) {
        return new TransactionThreadFactory(namePrefix, daemon);
    }

	public static boolean waitAllShutdown(int timeoutInMillis) {
        ThreadGroup group = getThreadGroup();
        Thread[] activeThreads = new Thread[group.activeCount()];
        group.enumerate(activeThreads);
        Set<Thread> alives = new HashSet<Thread>(Arrays.asList(activeThreads));
        Set<Thread> dies = new HashSet<Thread>();
        log.info("Current ACTIVE thread count is: {}", alives.size());
        long expire = System.currentTimeMillis() + timeoutInMillis;
        while (System.currentTimeMillis() < expire) {
        	classify(alives, dies, new ClassifyStandard<Thread>()
        	{
				@Override
				public boolean satisfy(Thread thread) {
					return !thread.isAlive() || thread.isInterrupted() || thread.isDaemon();
				}
        		
        	});
          
            if (alives.size() > 0) {
                log.info("Alive txTransaction threads: {}", alives);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {
                    // ignore
                }
            } else {
                log.info("All txTransaction threads are shutdown.");
                return true;
            }
        }
        log.warn("Some txTransaction threads are still alive but expire time has reached, alive threads: {}",
                alives);
        return false;
    }

    private interface ClassifyStandard<T> {
        /**
         * 没啥用
         *
         * @param thread 线程
         * @return boolean
         */
        boolean satisfy(T thread);
    }

    private static <T> void classify(Set<T> src, Set<T> des, ClassifyStandard<T> standard) {
        Set<T> set = new HashSet<>();
        for (T t : src) {
            if (standard.satisfy(t)) {
                set.add(t);
            }
        }
        src.removeAll(set);
        des.addAll(set);
    }

    private TransactionThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        TransactionThreadFactory.daemon = daemon;
    }


    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(THREAD_GROUP, runnable,
                THREAD_GROUP.getName() + "-" + namePrefix + "-" + threadNumber.getAndIncrement());
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
