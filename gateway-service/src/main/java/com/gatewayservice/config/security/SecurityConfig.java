package com.gatewayservice.config.security;

import com.erp.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import static com.erp.util.DataUtil.objectToXml;


@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtRequestFilter _jwtRequestFilter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Enable CORS and set the source
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/auth-service/login", "/auth-service/register").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(_jwtRequestFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public GlobalFilter customAuthHeaderFilter() {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    Authentication authentication = ctx.getAuthentication();
                    if (authentication != null && authentication.getPrincipal() instanceof UserDataContext userDataContext) {
                        log.info("ForwardAuth -> {}", objectToXml(userDataContext));
                        return chain.filter(exchange.mutate()
                                .request(r -> r.headers(h -> {
                                    h.add(Constant.FilterGatewayParams.X_USER_ID, String.valueOf(userDataContext.getUserId()));
                                    h.add(Constant.FilterGatewayParams.X_USER_NAME, userDataContext.getUserName());
                                    h.add(Constant.FilterGatewayParams.X_USER_ROLES, String.join(",", userDataContext.getRoles()));
                                }))
                                .build());
                    }
                    // nếu chưa có auth thì forward bình thường
                    return chain.filter(exchange);
                });
    }


}
