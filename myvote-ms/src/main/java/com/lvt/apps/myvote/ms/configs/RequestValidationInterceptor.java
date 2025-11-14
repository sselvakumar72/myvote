package com.lvt.apps.myvote.ms.configs;

mport com.optum.ofsc.bds.accounts.util.LoggerUtil;
import com.optum.ofsc.bds.accounts.validation.LogInjectionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

import static com.optum.ofsc.bds.accounts.validation.constants.ValidationConstants.AUTHORIZATION_HEADER;

@Component
@Slf4j
public class RequestValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!validateHeaders(request, response)) {
            return false;
        }
        if (!validateUri(request, response)) {
            return false;
        }
        if (!validateRequestParameters(request, response)) {
            return false;
        }
        return true;
    }

    private boolean validateHeaders(HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            if (AUTHORIZATION_HEADER.equalsIgnoreCase(header)) {
                continue; // Skip Authorization header
            }
            String value = request.getHeader(header);
            if (LogInjectionUtil.containsLogInjection(value)) {
                log.warn("Log injection attempt detected in header: {}", LoggerUtil.sanitize(value));
                addError(response);
                return false;
            }
        }
        return true;
    }

    private boolean validateUri(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        if (LogInjectionUtil.containsLogInjection(uri)) {
            log.warn("Log injection attempt detected in uri: {}", LoggerUtil.sanitize(uri));
            addError(response);
            return false;
        }
        return true;
    }

    private boolean validateRequestParameters(HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = paramNames.nextElement();
            String[] values = request.getParameterValues(param);
            if (values != null) {
                for (String value : values) {
                    if (LogInjectionUtil.containsLogInjection(value)) {
                        log.warn("Log injection attempt detected in request parameter '{}': {}",
                                LoggerUtil.sanitize(param), LoggerUtil.sanitize(value));
                        addError(response);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void addError(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}
