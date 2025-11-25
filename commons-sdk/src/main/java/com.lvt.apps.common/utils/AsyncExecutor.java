package com.lvt.apps.common.utils;

import com.lvt.apps.common.exceptions.AsyncExecutionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.util.function.ThrowingSupplier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AsyncExecutor {
    public static <T> CompletableFuture supplyAsync(ThrowingSupplier<T> supplier) {
        final Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();

        return CompletableFuture.supplyAsync(() -> {
            try {
                MDC.setContextMap(mdcContextMap);
                return supplier.get((string, exception) -> {
                    throw new AsyncExecutionException(exception);
                });
            } finally {
                MDC.clear();
            }
        });
    }
}
