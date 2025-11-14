package com.lvt.apps.myvote.ms.exceptions;

import com.optum.ofsc.bds.accounts.dto.fdx.FdxErrorCodes;
import com.optum.ofsc.bds.accounts.exception.AccountNotFoundException;
import com.optum.ofsc.bds.accounts.exception.BadRequestException;
import com.optum.ofsc.bds.accounts.exception.CoverageNotFoundException;
import com.optum.ofsc.bds.accounts.exception.CustomerNotFoundException;
import com.optum.ofsc.bds.accounts.exception.InvalidAccountIdException;
import com.optum.ofsc.bds.accounts.exception.PlanNotFoundException;
import com.optum.ofsc.bds.accounts.exception.UpstreamServiceException;
import com.optum.ofsc.bds.accounts.exception.constant.ErrorCodes;
import com.optum.ofsc.bds.accounts.exception.constant.ErrorMessages;
import com.optum.ofsc.bds.accounts.exception.util.ErrorHandlerUtil;
import com.optum.ofsc.bds.accounts.exception.UpstreamBadRequestException;
import com.optum.ofsc.bds.accounts.model.Error;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Enhanced global exception handler for the Accounts Service.
 * Handles exceptions and returns FDX-compliant error responses.
 * Enhanced to support Spring Boot Bean Validation exceptions from our custom annotations
 * and Spring Framework exceptions by extending ResponseEntityExceptionHandler.
 * Now integrates with RequestTrackingFilter for consistent request ID tracking.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles missing required request parameters (e.g., missing query parameters or required headers).
     * Returns FDX-compliant 400 error response.
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Missing required request parameter: {} [RequestId: {}]", ex.getParameterName(), requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                body(new Error()
                        .code(FdxErrorCodes.INVALID_INPUT)
                        .message(ErrorMessages.INVALID_INPUT)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles servlet request binding exceptions.
     * This can occur for issues like missing headers that are required.
     * Currently defers to the superclass implementation.
     */
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        String parameterName = null;
        if (ex instanceof MissingRequestHeaderException) {
            parameterName = ((MissingRequestHeaderException) ex).getHeaderName();
        } else if (ex instanceof MissingServletRequestParameterException) {
            parameterName = ((MissingServletRequestParameterException) ex).getParameterName();
        }

        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Request binding error [RequestId: {}]: parameter:{}, {}",
                requestId, parameterName, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_INPUT)
                        .message(ErrorMessages.INVALID_INPUT)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles malformed JSON or other message reading issues.
     * Returns FDX-compliant 400 error response.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("HTTP message not readable [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_PARAMETER)
                        .message(ErrorMessages.INVALID_REQUEST_FORMAT)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles method argument validation errors (e.g., @Valid annotation failures on request bodies).
     * Returns FDX-compliant 400 error response.
     * <p>
     * Note: This overrides the @ExceptionHandler version below to ensure Spring's built-in
     * exception handling takes precedence.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Method argument validation error [RequestId: {}]:{}",
                requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_PARAMETER)
                        .message(ErrorMessages.VALIDATION_FAILED)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles type mismatch errors for request parameters (e.g., invalid enum values).
     * Returns FDX-compliant 400 error response.
     */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
                                                        HttpHeaders headers,
                                                        HttpStatusCode status,
                                                        WebRequest request) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Type mismatch error [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_PARAMETER)
                        .message(ErrorMessages.VALIDATION_FAILED)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles ConstraintViolationException from method parameter validation.
     * This is thrown when our custom validation annotations (@ValidAccountIdentifier, @ValidCustomerId, etc.) fail.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Error> handleConstraintViolationException(ConstraintViolationException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();

        log.info("Constraint validation error [RequestId: {}]:{}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_PARAMETER)
                        .message(ErrorMessages.VALIDATION_FAILED)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }


    /**
     * Handles BadRequestException for all validation errors (including our new validation framework)
     */
    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<Error> handleBadRequestException(BadRequestException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Bad request validation error [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_INPUT)
                        .message(ErrorMessages.INVALID_INPUT)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles UpstreamBadRequestException for bad request sent from upstream services
     */
    @ExceptionHandler(UpstreamBadRequestException.class)
    ResponseEntity<Error> handleUpstreamBadRequestException(UpstreamBadRequestException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Bad request error [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_INPUT)
                        .message(ErrorMessages.INVALID_INPUT)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles AccountNotFoundException and returns FDX-compliant 404 response
     */
    @ExceptionHandler(AccountNotFoundException.class)
    ResponseEntity<Error> handleAccountNotFoundException(AccountNotFoundException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Account not found [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Error()
                        .code(FdxErrorCodes.ACCOUNT_NOT_FOUND)
                        .message(ErrorMessages.ACCOUNT_NOT_FOUND)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));

    }

    /**
     * Handles PlanNotFoundException and returns FDX-compliant 404 response
     */
    @ExceptionHandler(PlanNotFoundException.class)
    ResponseEntity<Error> handlePlanNotFoundException(PlanNotFoundException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Plan not found [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Error()
                        .code(FdxErrorCodes.ACCOUNT_NOT_FOUND)
                        .message(ErrorMessages.PLAN_NOT_FOUND)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles CoverageNotFoundException and returns FDX-compliant 404 response
     */
    @ExceptionHandler(CoverageNotFoundException.class)
    ResponseEntity<Error> handleCoverageNotFoundException(CoverageNotFoundException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Coverage information not found [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Error()
                        .code(FdxErrorCodes.ACCOUNT_NOT_FOUND)
                        .message(ErrorMessages.COVERAGE_INFORMATION_NOT_FOUND)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles CustomerNotFoundException and returns FDX-compliant 404 response
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    ResponseEntity<Error> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Customer not found [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Error()
                        .code(FdxErrorCodes.CUSTOMER_NOT_FOUND)
                        .message(ErrorMessages.CUSTOMER_NOT_FOUND)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles InvalidAccountIdException for backward compatibility
     */
    @ExceptionHandler(InvalidAccountIdException.class)
    ResponseEntity<Error> handleInvalidAccountIdException(InvalidAccountIdException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Invalid account ID [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_ACCOUNT_NUMBER_FORMAT)
                        .message(ErrorMessages.ACCOUNT_ID_FORMAT_IS_INVALID)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles IllegalArgumentException from request validators.
     * Returns FDX-compliant 400 error response for validation failures.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Error> handleIllegalArgumentException(IllegalArgumentException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.info("Validation error [RequestId: {}]: {}", requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Error()
                        .code(FdxErrorCodes.INVALID_INPUT)
                        .message(ErrorMessages.INVALID_INPUT)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles UpstreamServiceException and returns FDX-compliant 502 response
     */
    @ExceptionHandler(UpstreamServiceException.class)
    ResponseEntity<Error> handleUpstreamServiceException(UpstreamServiceException ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.error("Upstream service error [RequestId: {}]: {}", requestId, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Error()
                        .code(FdxErrorCodes.SUBSYSTEM_UNAVAILABLE_CODE)
                        .message(ErrorMessages.SUBSYSTEM_UNAVAILABLE_ERROR)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }

    /**
     * Handles generic exceptions and returns FDX-compliant 500 response
     * Enhanced with request tracking for better debugging
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<Error> handleGenericException(Exception ex) {
        String requestId = ErrorHandlerUtil.getRequestId();
        log.error("Unexpected error occurred [RequestId: {}]: {}", requestId, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Error()
                        .code(ErrorCodes.INTERNAL_SERVER_ERROR_CODE)
                        .message(ErrorMessages.INTERNAL_SERVER_ERROR)
                        .debugMessage(ErrorHandlerUtil.getDebugMessage(requestId)));
    }
}
