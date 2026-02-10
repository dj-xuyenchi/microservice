package com.gatewayservice.service;

import reactor.core.publisher.Mono;

public interface IRoleService {
    void getApiAndListRoleActiveAndWhiteListApi();
    Mono<Void> reloadApiAndRoleCache();
}
