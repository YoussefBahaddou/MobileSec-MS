package com.mobilesec.getaway_service.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ApiKeyAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String API_KEY_HEADER = "X-API-KEY";
    // For MVP, hardcoded key. In production, use properties or DB.
    private static final String VALID_API_KEY = "ms-ci-secret-key-123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Only apply to CI endpoints
        if (path.startsWith("/api/ci/")) {
            List<String> apiKeyHeader = exchange.getRequest().getHeaders().get(API_KEY_HEADER);
            if (apiKeyHeader == null || apiKeyHeader.isEmpty() || !VALID_API_KEY.equals(apiKeyHeader.get(0))) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
