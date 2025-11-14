package com.lvt.apps.myvote.ms.exceptions;

import lombok.Getter;

@Getter
abstract class BaseException extends Exception {

    private final MyVoteError error;

    public BaseException(MyVoteError error) {
        super(error.getMessage());
        this.error = error;
    }

    public MyVoteError getError() {
        return error;
    }
}
