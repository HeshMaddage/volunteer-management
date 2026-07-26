package com.volunteer.management.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits repeated calls to auth endpoints per client IP, to slow down
 * brute-force login/registration attempts. Each IP gets its own bucket of
 * 5 requests, refilling at 5 per minute.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limiting.enabled:true}")
    private boolean enabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isAuthEndpoint = path.startsWith("/api/v1/auth/");

        if (!enabled || !isAuthEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests, please try again later\"}");
        }
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(5, io.github.bucket4j.Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}