package com.gatewayservice.service.impl;

import com.erp.commonservice.RedisService;
import com.erp.model.ApiUri;
import com.gatewayservice.dto.RoleUriDTO;
import com.gatewayservice.service.IRoleService;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gatewayservice.constant.RequestGatewayApi.*;


@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final JdbcTemplate _jdbcTemplate;
    private final RedisService redisService;

    @Override
    @Transactional
    public void getApiAndListRoleActiveAndWhiteListApi() {
        Map<String, List<RoleUriDTO>> res = null;
        List<ApiUri> listActiveApi = getActiveApi();
        List<ApiUri> whiteListApi = getWhiteListApi();
        redisService.saveWithTTL(API_URI, listActiveApi);
        redisService.saveWithTTL(WHITE_LIST_API, whiteListApi);
        String query = """
                SELECT * FROM pr_get_role_mapping_uri()
                """;
        List<RoleUriDTO> lst = _jdbcTemplate.query(query, new BeanPropertyRowMapper<>(RoleUriDTO.class));
        res = lst.stream()
                .collect(Collectors.groupingBy(RoleUriDTO::getUri, Collectors.mapping(r -> r, Collectors.toList())));
        redisService.saveWithTTL(SYSTEM_ROLE, res);
    }

    private List<ApiUri> getActiveApi() {
        String query = "SELECT * FROM API_URI WHERE STATUS = 'O'";
        List<ApiUri> list = _jdbcTemplate.query(query, new BeanPropertyRowMapper<>(ApiUri.class));
        return list;
    }

    private List<ApiUri> getWhiteListApi() {
        String query = "SELECT * FROM API_URI WHERE IS_WHITE_END_POINT ='t'";
        List<ApiUri> list = _jdbcTemplate.query(query, new BeanPropertyRowMapper<>(ApiUri.class));
        return list;
    }
}
