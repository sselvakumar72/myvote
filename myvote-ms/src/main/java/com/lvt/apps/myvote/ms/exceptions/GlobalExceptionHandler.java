package com.lvt.apps.myvote.ms.exceptions;


import com.lvt.apps.myvote.ms.constants.MyVoteConstants;
import com.lvt.apps.myvote.ms.exceptions.constants.ErrorCodes;
import com.lvt.apps.myvote.ms.exceptions.constants.ErrorMessages;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Enhanced global exception handler for the Accounts Service.
 * Handles exceptions and returns FDX-compliant MyVoteError responses.
 * Enhanced to support Spring Boot Bean Validation exceptions from our custom annotations
 * and Spring Framework exceptions by extending ResponseEntityExceptionHandler.
 * Now integrates with RequestTrackingFilter for consistent request ID tracking.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles missing required request parameters (e.g., missing query parameters or required headers).
     * Returns FDX-compliant 400 MyVoteError response.
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Missing required request parameter: {} [RequestId: {}]", ex.getParameterName(), requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                body(MyVoteError.builder()
                        .code(ErrorCodes.INVALID_INPUT_CODE)
                        .message(ErrorMessages.INVALID_INPUT)
                        .description(MyVoteConstants.EMPTY_STRING).build());
    }


}
