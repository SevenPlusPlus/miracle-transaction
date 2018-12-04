package com.miracle.common.transaction.exception;


public class TccRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -1949770547060521702L;

    public TccRuntimeException() {
    }

    public TccRuntimeException(String message) {
        super(message);
    }

    public TccRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TccRuntimeException(Throwable cause) {
        super(cause);
    }
}
