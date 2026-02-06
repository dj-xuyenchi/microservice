package com.gatewayservice.config.security;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép cả localhost lẫn IP
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /*
    Xóa Header trùng khi bắn trả client
     */
    @Bean
    public GlobalFilter removeDuplicateCorsHeaders() {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            var requestOrigin = exchange.getRequest().getHeaders().getOrigin();
            var headers = exchange.getResponse().getHeaders();
            if (requestOrigin != null) {
                headers.setAccessControlAllowOrigin(requestOrigin);
            } // hoặc lấy từ request Origin
        }));
    }
}
