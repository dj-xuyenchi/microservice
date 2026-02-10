package com.erp.authenservice.util;

import com.erp.authenservice.config.RedisPool;
import com.erp.authenservice.service.IGlobalConfigService;
import com.erp.constant.Constant;
import com.erp.model.GlobalConfig;
import com.erp.model.GlobalConfigData;
import com.erp.vo.UserDataContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.erp.util.DataUtil.isNullOrEmpty;

@Slf4j
@Component
public class SystemContext {

    private static RedisPool _redisPool;
    private static IGlobalConfigService _globalService;

    public static UserDataContext getContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserDataContext) auth.getPrincipal();
    }

    @Autowired
    public void setRedis(RedisPool redisPool) {
        SystemContext._redisPool = redisPool;
    }

    @Autowired
    public void setGlobalService(IGlobalConfigService globalService) {
        SystemContext._globalService = globalService;
    }

    public static void resetGlobalConfig() {
        try {
            _redisPool.delete(Constant.GlobalConfig.GLOBAL);
            _redisPool.delete(Constant.GlobalConfig.GLOBAL_DATA);
        } catch (Exception e) {
            log.error("//ERROR -> {}", ExceptionUtils.getStackTrace(e));
        }
    }

    public static GlobalConfig getGlobalConfig(String code) {
        try {
            List<GlobalConfig> globalConfigResponseList = _redisPool.getObjectList(Constant.GlobalConfig.GLOBAL, GlobalConfig.class);
            if (isNullOrEmpty(globalConfigResponseList)) {
                globalConfigResponseList = _globalService.getAllActiveGlobalConfig();
                _redisPool.saveWithTTL(Constant.GlobalConfig.GLOBAL, globalConfigResponseList);
            }
            GlobalConfig res = globalConfigResponseList.stream().filter(g -> {
                return g.getGlobalConfigCode().equals(code);
            }).findFirst().orElse(null);
            log.info("//Code -> {}", code);
            return res;
        } catch (Exception e) {
            log.error("//ERROR -> {}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public static GlobalConfigData getGlobalConfigData(String code) {
        try {
            List<GlobalConfigData> glData = _redisPool.getObjectList(Constant.GlobalConfig.GLOBAL_DATA, GlobalConfigData.class);
            if (isNullOrEmpty(glData)) {
                glData = _globalService.getAllActiveGlobalConfigData();
                _redisPool.saveWithTTL(Constant.GlobalConfig.GLOBAL_DATA, glData);
            }
            GlobalConfigData res = glData.stream().filter(g -> {
                return g.getGlobalConfigDataCode().equals(code);
            }).findFirst().orElse(null);
            log.info("//Code -> {}", code);
            return res;
        } catch (Exception e) {
            log.error("//ERROR -> {}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public static List<GlobalConfigData> getGlobalConfigDataByGl(Long glId) {
        try {
            List<GlobalConfigData> glData = _redisPool.getObjectList(Constant.GlobalConfig.GLOBAL_DATA, GlobalConfigData.class);
            if (isNullOrEmpty(glData)) {
                glData = _globalService.getAllActiveGlobalConfigData();
                _redisPool.saveWithTTL(Constant.GlobalConfig.GLOBAL_DATA, glData);
            }
            List<GlobalConfigData> res = glData.stream().filter(g -> {
                return g.getGlobalConfigId().equals(glId);
            }).toList();
            log.info("//Code -> {}", glId);
            return res;
        } catch (Exception e) {
            log.error("//ERROR -> {}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

}

