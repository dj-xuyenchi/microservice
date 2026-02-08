package com.erp.authenservice.service;


import com.erp.authenservice.dto.response.GlobalConfigDataResponse;
import com.erp.authenservice.dto.response.GlobalConfigResponse;
import com.erp.model.GlobalConfig;
import com.erp.model.GlobalConfigData;

import java.util.List;

public interface IGlobalConfigService {
    GlobalConfigResponse getGlobalConfigById(Long id);

    List<GlobalConfigDataResponse> getGlobalConfigDataByConfigCode(String configCode);

    List<GlobalConfig> getAllActiveGlobalConfig();

    List<GlobalConfigData> getAllActiveGlobalConfigData();
}
