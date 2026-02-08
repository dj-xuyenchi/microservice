package com.erp.authenservice.service.impl;

import com.erp.authenservice.dto.response.GlobalConfigDataResponse;
import com.erp.authenservice.dto.response.GlobalConfigResponse;
import com.erp.authenservice.service.IGlobalConfigService;
import com.erp.model.GlobalConfig;
import com.erp.model.GlobalConfigData;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.erp.util.DataUtil.isNull;


@Service
@RequiredArgsConstructor
public class GlobalConfigServiceImpl implements IGlobalConfigService {
//    private final IGlobalConfigRepo _globalConfigRepo;
//    private final IGlobalConfigDataRepo _globalConfigDataRepo;
    private final NamedParameterJdbcTemplate _namedParameterJdbcTemplate;

    @Override
    public GlobalConfigResponse getGlobalConfigById(Long id) {
        GlobalConfig globalConfig =null;
//        GlobalConfig globalConfig = _globalConfigRepo.findById(id).orElse(null);
        if (isNull(globalConfig)) {
            return null;
        }
        return GlobalConfigResponse.builder()
                .globalConfigId(globalConfig.getGlobalConfigId())
                .globalConfigName(globalConfig.getGlobalConfigName())
                .globalConfigValue(globalConfig.getGlobalConfigValue())
                .globalConfigCode(globalConfig.getGlobalConfigCode())
                .status(globalConfig.getStatus())
                .globalConfigDescription(globalConfig.getGlobalConfigDescription())
                .createdBy(globalConfig.getCreatedBy())
                .build();
    }

    @Override
    @Transactional
    public List<GlobalConfigDataResponse> getGlobalConfigDataByConfigCode(String configCode) {
        String query = """
                {CALL pr_get_Global_Config_Data_By_Config_Code(:configCode)}
                """;
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("configCode", configCode);
        List<GlobalConfigDataResponse> res = _namedParameterJdbcTemplate.query(query, param, new BeanPropertyRowMapper<>(GlobalConfigDataResponse.class));
        return res;
    }

    @Override
    public List<GlobalConfig> getAllActiveGlobalConfig() {
//        return _globalConfigRepo.getAllActiveGlobalConfig();
        return null;
    }

    @Override
    public List<GlobalConfigData> getAllActiveGlobalConfigData() {
        return null;
//        return _globalConfigDataRepo.getAllActiveGlobalConfigData();
    }
}
