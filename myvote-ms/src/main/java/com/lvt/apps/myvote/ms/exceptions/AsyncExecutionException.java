package com.lvt.apps.myvote.ms.exceptions;

public class AsyncExecutionException extends BaseRuntimeException{

    private final Exception erorrCause;

    public AsyncExecutionException(Exception erorrCause)   {
        super(erorrCause);
        this.erorrCause = erorrCause;
    }
}
