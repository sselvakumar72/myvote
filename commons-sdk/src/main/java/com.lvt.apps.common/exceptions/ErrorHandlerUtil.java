package com.lvt.apps.common.exceptions;


import com.lvt.apps.common.exceptions.constants.ErrorCodes;
import com.lvt.apps.common.exceptions.constants.ErrorMessages;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static com.lvt.apps.common.configs.LoggingFilter.MDC_REQUEST_CORRELATION_ID_KEY;


@UtilityClass
public class ErrorHandlerUtil {

    private static final String DEBUG_MESSAGE = "Please share this correlationId with the team if " +
            "you need assistance. CorrelationId: %s.";

    /**
     * Gets the request ID from MDC if available, otherwise generates a new UUID.
     * This ensures consistency with the RequestTrackingFilter.
     */
    public static String getRequestId() {
        String requestId = MDC.get(MDC_REQUEST_CORRELATION_ID_KEY);
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    /**
     * Constructs the debug message including the request ID if in a non-production profile.
     * Otherwise, returns null to omit the debug message.
     */
    public static String getDebugMessage(String requestId) {
        return DEBUG_MESSAGE.formatted(requestId);
    }

    /**
     * Creates a standardized bad request Error object.
     *
     * @return Error object representing a bad request
     */
    public static MyVoteError badRequest() {
        return MyVoteError.builder()
                .code(ErrorCodes.INVALID_INPUT_CODE)
                .message(ErrorMessages.INVALID_INPUT)
                .build();
    }
}
