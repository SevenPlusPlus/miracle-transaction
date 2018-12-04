package com.miracle.common.transaction.api;

public class LogTransactionContextLocal {

    private static final ThreadLocal<LogTransactionContext> CURRENT_LOCAL = new ThreadLocal<>();

    private static final LogTransactionContextLocal TRANSACTION_CONTEXT_LOCAL = new LogTransactionContextLocal();

    private LogTransactionContextLocal() {

    }

    public static LogTransactionContextLocal getInstance() {
        return TRANSACTION_CONTEXT_LOCAL;
    }


    public void set(LogTransactionContext context) {
        CURRENT_LOCAL.set(context);
    }

    public LogTransactionContext get() {
        return CURRENT_LOCAL.get();
    }

    public void remove() {
        CURRENT_LOCAL.remove();
    }
}
