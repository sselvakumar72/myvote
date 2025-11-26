package com.lvt.apps.common.configs;

import com.lvt.apps.common.logging.CachedBodyHttpServletRequest;
import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.Map;
import java.util.UUID;

/**
 * Default OncePerRequestFilter to log request information.
 * Enhanced to properly handle x-fapi-interaction-id and MDC integration.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {
    private static final String REQUEST_CORRELATION_ID_HEADER_KEY = "X-Interaction-Id";
    public static final String MDC_REQUEST_CORRELATION_ID_KEY = "X-Correlation-Id";
    public static final String OUTBOUND_AUDIT_KEY = "LVT-MYVOTE-AUDIT-902";
    public static final String INBOUND_AUDIT_KEY = "LVT-MYVOTE-AUDIT-901";

    private static String getCorrelationId(@NonNull CachedBodyHttpServletRequest request) {
        final String correlationId = request.getHeader(REQUEST_CORRELATION_ID_HEADER_KEY);
        if (StringUtils.isEmpty(correlationId)) {
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }

    private static void logInboundOperation(@NonNull CachedBodyHttpServletRequest request) {
        String url = getFullURL(request);
        String requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

        Map<String, String> logParam = new HashMap<>();
        logParam.put("INBOUND_AUDIT_KEY", INBOUND_AUDIT_KEY);
        logParam.put("url", url);
        logParam.put("httpMethod", request.getMethod());

        logParam.put("headers", getRequestHeaders(request).toString());

        if (!StringUtils.isEmpty(requestBody)) {
            logParam.put("requestBody", requestBody);
        }

        log.info("Inbound request info", StructuredArguments.e(logParam));
    }

    @NotNull
    private static Map<String, String> getRequestHeaders(@NotNull CachedBodyHttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!"authorization".equalsIgnoreCase(headerName)
                    && !REQUEST_CORRELATION_ID_HEADER_KEY.equalsIgnoreCase(headerName)) {
                headers.put(headerName, request.getHeader(headerName));
            }
        });
        return headers;
    }

    private static void logOutboundOperation(@NonNull CachedBodyHttpServletRequest request,
                                             @NonNull ContentCachingResponseWrapper response, long executionTime) {
        String url = getFullURL(request);
        String responseBody = getStringValue(response.getContentAsByteArray(), response.getCharacterEncoding());
        int httpStatus = response.getStatus();

        Map<String, String> logParam = new HashMap<>();
        logParam.put("OUTBOUND_AUDIT_KEY", OUTBOUND_AUDIT_KEY);
        logParam.put("url", url);
        logParam.put("httpMethod", request.getMethod());
        logParam.put("httpStatus", String.valueOf(httpStatus));
        logParam.put("executionTime", String.valueOf(executionTime));

        if (!StringUtils.isEmpty(responseBody)) {
            logParam.put("responseBody", responseBody);
        }

        log.info("Outbound response info", StructuredArguments.e(logParam));
    }

    private static String getFullURL(@NonNull CachedBodyHttpServletRequest request) {
        if (request.getQueryString() == null) {
            return request.getRequestURI();
        } else {
            String uri = request.getRequestURI();
            return uri + "?" + request.getQueryString();
        }
    }

    private static String getStringValue(byte[] contentAsByteArray, @NonNull String characterEncoding) {

        try {
            return new String(contentAsByteArray, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            log.error("[AuditLogger], Unable to get StringValue.", e);
            return "";
        }
    }

    private static void cleanMDCCorrelationId() {
        MDC.remove(MDC_REQUEST_CORRELATION_ID_KEY); // Updated to use consistent key
    }

    private static void addCorrelationIdToMDC(CachedBodyHttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        MDC.put(MDC_REQUEST_CORRELATION_ID_KEY, correlationId); // Updated to use consistent key
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CachedBodyHttpServletRequest contentCachingRequestWrapper = new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);

        try {
            addCorrelationIdToMDC(contentCachingRequestWrapper);
            logInboundOperation(contentCachingRequestWrapper);
            StopWatch watch = new StopWatch();
            watch.start();

            filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);

            watch.stop();
            logOutboundOperation(contentCachingRequestWrapper, contentCachingResponseWrapper,
                    watch.getTotalTimeMillis());

            contentCachingResponseWrapper.copyBodyToResponse();

        } finally {
            cleanMDCCorrelationId();
        }
    }
}
