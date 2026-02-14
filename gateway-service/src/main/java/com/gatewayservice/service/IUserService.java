package com.gatewayservice.service;

import java.util.List;

public interface IUserService {
    List<String> getUserRole(String userName);
}
