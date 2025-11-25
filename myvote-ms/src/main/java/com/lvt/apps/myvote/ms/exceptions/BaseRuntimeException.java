package com.lvt.apps.myvote.ms.exceptions;

public class BaseRuntimeException extends RuntimeException{

    public BaseRuntimeException(Exception exception) {
        super(exception);
    }

    public BaseRuntimeException(String exception) {
        super(exception);
    }

    public BaseRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
