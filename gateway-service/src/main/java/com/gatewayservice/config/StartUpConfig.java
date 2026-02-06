package com.gatewayservice.config;

import com.gateway.service.impl.RoleServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StartUpConfig {
    private final RoleServiceImpl roleServiceImpl;

    @PostConstruct
    public void sexyLoadConfig() {
        roleServiceImpl.getApiAndListRoleActiveAndWhiteListApi();
    }
}
