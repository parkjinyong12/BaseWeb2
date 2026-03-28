package com.ruokit.baseweb.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        request.setAttribute(TRACE_ID, traceId);
        response.setHeader("X-Trace-Id", traceId);

        MDC.put(TRACE_ID, traceId);
        try {
            logApiEntry(request);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }

    private void logApiEntry(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.startsWith("/api/")) {
            String queryString = request.getQueryString();
            String fullPath = queryString == null ? requestUri : requestUri + "?" + queryString;
            log.info("API entry: method={}, path={}", request.getMethod(), fullPath);
        }
    }
}
