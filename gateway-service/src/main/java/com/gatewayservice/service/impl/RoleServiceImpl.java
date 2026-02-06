package com.gatewayservice.service.impl;

import com.gateway.config.RedisPool;
import com.gateway.entity.ApiUri;
import com.gateway.entity.SystemApplication;
import com.gateway.repository.IApiUriRepo;
import com.gateway.repository.IRoleApplyRepo;
import com.gateway.repository.ISystemApplicationRepo;
import com.gateway.repository.ISystemRoleRepo;
import com.gateway.service.IRoleService;
import dto.RoleUriDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static constant.AuthServiceConstant.*;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {
    private final ISystemRoleRepo _systemRoleRepo;
    private final IApiUriRepo _apiUriRepo;
    private final ISystemApplicationRepo _applicationRepo;
    private final IRoleApplyRepo _roleApplyRepo;
    private final JdbcTemplate _jdbcTemplate;
    private final RedisPool _redisPool;

    @Override
    @Transactional
    public void getApiAndListRoleActiveAndWhiteListApi() {
        Map<String, List<RoleUriDTO>> res = null;
        List<ApiUri> listActiveApi = _apiUriRepo.findAllByStatusEquals("O");
        List<ApiUri> whiteListApi = _apiUriRepo.findAllByIsWhiteEndPoint(true);
        List<SystemApplication> services = _applicationRepo.findAllByStatusEquals("O");
        _redisPool.saveWithTTL(API_URI, listActiveApi);
        _redisPool.saveWithTTL(WHITE_LIST_API, whiteListApi);
        _redisPool.saveWithTTL(SYSTEM_SERVICE, services);
        String query = """
                SELECT * FROM pr_get_role_mapping_uri()
                """;
        List<RoleUriDTO> lst = _jdbcTemplate.query(query, new BeanPropertyRowMapper<>(RoleUriDTO.class));
        res = lst.stream()
                .collect(Collectors.groupingBy(RoleUriDTO::getUri, Collectors.mapping(r -> r, Collectors.toList())));
        _redisPool.saveWithTTL(SYSTEM_ROLE, res);
    }
}
