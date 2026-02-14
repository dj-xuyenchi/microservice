package com.gatewayservice.service;

import reactor.core.publisher.Mono;

public interface IRoleService {
    Mono<Void>  getApiAndListRoleActiveAndWhiteListApi();

}
