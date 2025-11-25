package com.lvt.apps.myvote.ms.logging;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
abstract class RequestLoggingFilter extends OncePerRequestFilter {
    public static final String LOG_PATTERN_CORRELATION_ID_KEY = "X-CID";
    public static final String CORRELATION_ID_KEY = "X-Correlation-Id";
    private static final String INPUT_AUDIT_KEY = "FARO-AUDIT-FARO-901";
    private static final String OUTPUT_AUDIT_KEY = "FARO-AUDIT-FARO-902";
    private static final String OID = "OID";
    private static final String ACTOR = "actor";
/*    *//**
     * Method that gets the correlationId from the request headers or create a new correlationId for the request.
     *
     * @param headers The headers of the request.
     * @return The correlationId for the request.
     *//*
    private static String getCorrelationId(@NonNull AuditHeader headers) {
        if (StringUtils.isEmpty(headers.getCorrelationId())) {
            return java.util.UUID.randomUUID().toString();
        }

        return headers.getCorrelationId();
    }

    *//**
     * As part of our logs, we want to include an extended version of the correlationId to provide extra information.
     * This extended version will have the format: [{UUID/FARO_ID,{CorrelationId},actor={actor}}]
     * For example, [0lksds-4f20-4ccb-9a470-9eb56f,183de796-4147-40d4-9a17-837a2d71726d,actor=bank-api-gateway.optum.com]
     * <p>
     * This method is in change of building the extended version of our correlation id.
     *
     * @param headers       The headers received in the input request.
     * @param correlationId The correlationId for the request.
     * @return The extended version of the correlationId.
     *//*
    private static String getExtendedCorrelationId(@NonNull AuditHeader headers,
                                                   @NonNull String correlationId) {

        return LoggingCorrelationId.builder()
                .uuid(headers.getUserId())
                .faroId(headers.getFaroId())
                .clientCorrelationId(headers.getCorrelationId())
                .newCorrelationId(correlationId)
                .actor(headers.getActor())
                .hcpAuthorizationToken(headers.getHcpAuthorizationToken())
                .build()
                .toString();
    }

    private static void logInboundOperation(@NonNull CachedBodyHttpServletRequest request) {

        final String url = getFullURL(request);
        final String requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

        if (StringUtils.isEmpty(requestBody)) {
            log.info("{} Inbound Request 2: {} ", INPUT_AUDIT_KEY, url);
        } else {
            log.info("{} Inbound Request 1: {} , Payload: {}", INPUT_AUDIT_KEY, url, requestBody);
        }
    }

    private static void logOutboundOperation(@NonNull CachedBodyHttpServletRequest request,
                                             @NonNull ContentCachingResponseWrapper response,
                                             long executionTime) {

        final String url = getFullURL(request);
        String responseBody = getStringValue(response.getContentAsByteArray(), response.getCharacterEncoding());
        int httpStatus = response.getStatus();

        if (HttpStatus.OK.value() == httpStatus) {
            log.info("{} Outbound Response:{}- Processing Time:{} - Http Status:{}", OUTPUT_AUDIT_KEY, url, executionTime, httpStatus);
        } else {
            if (StringUtils.isEmpty(responseBody)) {
                log.info("{} Outbound Response:{}- Processing Time:{} - Http Status:{} - Outbound Response: Null", OUTPUT_AUDIT_KEY, url, executionTime, httpStatus);
            } else {
                log.info("{} Outbound Response:{}- Processing Time:{} - Http Status:{} - Outbound Response::{}", OUTPUT_AUDIT_KEY, url, executionTime, httpStatus, responseBody);
            }
        }
    }

    private static String getFullURL(@NonNull CachedBodyHttpServletRequest request) {
        if (request.getQueryString() == null) {
            return request.getRequestURI();
        }

        return request.getRequestURI() + '?' + request.getQueryString();
    }

    // method used to get string  value of request body
    private static String getStringValue(byte[] contentAsByteArray,
                                         @NonNull String characterEncoding) {
        try {
            return new String(contentAsByteArray, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            log.error("[AuditLogger], Unable to get StringValue.", e);
            return "";
        }
    }

    private static void cleanMDCCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(LOG_PATTERN_CORRELATION_ID_KEY);
        MDC.remove(OID);
        MDC.remove(ACTOR);
    }

    private static void addCorrelationIdToMDC(AuditHeader headers) {

        // Each request in FARO should have a unique correlation id that identifies it.
        final String correlationId = getCorrelationId(headers);

        // But also for each request, we create an extended version of the correlationId
        // That includes not only the id for the request but also, information about the user
        // and actor executing the request.
        final String extendedCorrelationId = getExtendedCorrelationId(headers, correlationId);

        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(LOG_PATTERN_CORRELATION_ID_KEY, extendedCorrelationId);
        if (Objects.nonNull(headers.getHcpAuthorizationToken()) && Objects.nonNull(headers.getHcpAuthorizationToken().getOid())) {
            MDC.put(OID, headers.getHcpAuthorizationToken().getOid());
        } else if (Objects.nonNull(headers.getActor())) {
            log.info("requestloggingfilter actor: {}", headers.getActor());
            MDC.put(ACTOR, headers.getActor());
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final CachedBodyHttpServletRequest contentCachingRequestWrapper = new CachedBodyHttpServletRequest(request);
        final ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        final AuditHeader headers = new AuditHeader(contentCachingRequestWrapper);

        try {
            addCorrelationIdToMDC(headers);
            logInboundOperation(contentCachingRequestWrapper);

            final StopWatch watch = new StopWatch();
            watch.start();
            filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);
            watch.stop();

            logOutboundOperation(contentCachingRequestWrapper, contentCachingResponseWrapper, watch.getTotalTimeMillis());
            contentCachingResponseWrapper.copyBodyToResponse();
        } finally {
            cleanMDCCorrelationId();
        }
    }*/
}
