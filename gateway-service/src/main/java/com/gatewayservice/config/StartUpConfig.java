package com.gatewayservice.config;

import com.gatewayservice.service.impl.RoleServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class StartUpConfig {
    private final RoleServiceImpl roleServiceImpl;

    @PostConstruct
    public void sexyLoadConfig() {
//        roleServiceImpl
//                .getApiAndListRoleActiveAndWhiteListApi()
//                .subscribe();
    }
}
