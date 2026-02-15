package com.erp.authenservice.service.impl;

import com.erp.authenservice.dto.response.BtnRoleResponse;
import com.erp.authenservice.service.AuthenService;
import com.erp.model.CatApi;
import com.erp.util.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenServiceImpl implements AuthenService {
    private final JdbcTemplate _jdbcTemplate;

    @Override
    public List<CatApi> testTai() {
        try (Connection conn = _jdbcTemplate.getDataSource().getConnection()) {

            DatabaseMetaData meta = conn.getMetaData();

            log.info("DB URL: {}", meta.getURL());
            log.info("DB User: {}", meta.getUserName());
            log.info("DB Product: {}", meta.getDatabaseProductName());
            log.info("DB Version: {}", meta.getDatabaseProductVersion());

        } catch (SQLException e) {
            log.error("Cannot get DB info", e);
        }
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
