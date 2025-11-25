package com.lvt.apps.common.exceptions;

public class AsyncExecutionException extends BaseRuntimeException{

    private final Exception erorrCause;

    public AsyncExecutionException(Exception erorrCause)   {
        super(erorrCause);
        this.erorrCause = erorrCause;
    }
}
