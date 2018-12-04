package com.miracle.common.transaction.exception;


public class TccException extends Exception {
    private static final long serialVersionUID = -948934144333391208L;

    public TccException() {
    }

    public TccException(String message) {
        super(message);
    }

    public TccException(String message, Throwable cause) {
        super(message, cause);
    }

    public TccException(Throwable cause) {
        super(cause);
    }
}
