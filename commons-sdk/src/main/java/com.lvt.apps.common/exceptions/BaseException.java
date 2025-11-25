package com.lvt.apps.common.exceptions;

import lombok.Getter;

@Getter
abstract class BaseException extends Exception {

    private final MyVoteError error;

    public BaseException(MyVoteError error) {
        super(error.getMessage());
        this.error = error;
    }

}
