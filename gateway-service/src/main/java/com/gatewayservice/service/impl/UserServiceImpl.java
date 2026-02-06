package com.gatewayservice.service.impl;

import com.gateway.repository.IRoleApplyRepo;
import com.gateway.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final IRoleApplyRepo _roleApplyRepo;

    @Override
    @Transactional
    public List<String> getUserRole(Long userId) {
        return _roleApplyRepo.getUserRole(userId);
    }
}
