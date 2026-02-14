package com.gatewayservice.service.impl;

import com.erp.model.CatApi;
import com.gatewayservice.config.RedisGateWayService;
import com.gatewayservice.dto.RoleUriDTO;
import com.gatewayservice.service.IRoleService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gatewayservice.constant.RequestGatewayApi.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final JdbcTemplate _jdbcTemplate;
    private final RedisGateWayService redisService;

    @Override
    public Mono<Void> getApiAndListRoleActiveAndWhiteListApi() {
        Map<String, List<RoleUriDTO>> res = null;
        List<CatApi> listActiveCatApi = getActiveApi();
        List<CatApi> whiteListCatApi = getWhiteListApi();
        String query = """
                SELECT * FROM public.fn_get_uri_mapping_role()
                """;
        List<RoleUriDTO> lst = _jdbcTemplate.query(query, new BeanPropertyRowMapper<>(RoleUriDTO.class));
        res = lst.stream()
                .collect(Collectors.groupingBy(r -> r.getUri() + "_" + r.getSystemId(), Collectors.mapping(r -> r, Collectors.toList())));
        return redisService.saveWithTTL(API_URI, listActiveCatApi)
                .then(redisService.saveWithTTL(WHITE_LIST_API, whiteListCatApi))
                .then(redisService.saveWithTTL(SYSTEM_ROLE, res));
    }

    private List<CatApi> getActiveApi() {
        String query = "SELECT * FROM PUBLIC.CAT_API WHERE STATUS = 'O'";
        List<CatApi> list = _jdbcTemplate.query(query, new BeanPropertyRowMapper<>(CatApi.class));
        log.info("//getActiveApi -> {}", list);
        return list;
    }

    private List<CatApi> getWhiteListApi() {
        String query = "SELECT * FROM PUBLIC.CAT_API WHERE IS_WHITE_END_POINT = 't'";
        List<CatApi> list = _jdbcTemplate.query(query, new BeanPropertyRowMapper<>(CatApi.class));
        log.info("//getWhiteListApi -> {}", list);
        return list;
    }
}
