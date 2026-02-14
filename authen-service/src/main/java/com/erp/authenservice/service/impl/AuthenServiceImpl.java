package com.erp.authenservice.service.impl;

import com.erp.authenservice.dto.response.BtnRoleResponse;
import com.erp.authenservice.service.AuthenService;
import com.erp.model.CatApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenServiceImpl implements AuthenService {
    private final JdbcTemplate _jdbcTemplate;

    @Override
    public List<CatApi> testTai() {
        String query = "SELECT * FROM PUBLIC.CAT_API WHERE STATUS = 'O'";
        List<CatApi> list = _jdbcTemplate.query(query, new BeanPropertyRowMapper<>(CatApi.class));
        log.info("//getActiveApi -> {}", list);
        return list;
    }

    @Override
    public BtnRoleResponse getBtnRoleForUser() {
        return null;
    }
}
