package com.lvt.apps.common.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidPageSizeException extends BaseRuntimeException {

    public InvalidPageSizeException(String message) {
        super(message);
        log.error("InvalidPageSizeException: {}", message);
    }
}
