package com.miracle.common.transaction.api;

public class TccTransactionContextLocal {


    private static final ThreadLocal<TccTransactionContext> CURRENT_LOCAL = new ThreadLocal<>();

    private static final TccTransactionContextLocal TRANSACTION_CONTEXT_LOCAL = new TccTransactionContextLocal();

    private TccTransactionContextLocal() {

    }

    public static TccTransactionContextLocal getInstance() {
        return TRANSACTION_CONTEXT_LOCAL;
    }


    public void set(TccTransactionContext context) {
        CURRENT_LOCAL.set(context);
    }

    public TccTransactionContext get() {
        return CURRENT_LOCAL.get();
    }

    public void remove() {
        CURRENT_LOCAL.remove();
    }
}
