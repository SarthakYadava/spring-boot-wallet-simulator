package com.sarth.walletsim.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String TRACKING_ID_KEY = "trackingId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Check if the request already has a tracking ID, otherwise integrate our one.
            String trackingId = request.getHeader("X-Tracking-Id");
            if (trackingId == null || trackingId.isEmpty()) {
                trackingId = UUID.randomUUID().toString();
            }

            // Store the request ID in MDC so each log line for this request includes the same tracking value.
            MDC.put(TRACKING_ID_KEY, trackingId);

            // Add it to the response header so the client/frontend knows the ID
            response.setHeader("X-Tracking-Id", trackingId);

            filterChain.doFilter(request, response);
        } finally {
            // Clear MDC after the request is finished.
            // Since Spring uses a Thread Pool, failing to clear this
            // would cause the ID to "leak" into the next user's request.
            MDC.remove(TRACKING_ID_KEY);
        }
    }
}
