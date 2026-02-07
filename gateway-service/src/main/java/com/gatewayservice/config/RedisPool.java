package com.gatewayservice.config;


import com.erp.commonservice.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;


@Component
@RequiredArgsConstructor
public class RedisPool {
    @Configuration
    public class CommonConfig {

        @Bean
        public RedisService redisService(JedisPool jedisPool) {
            return new RedisService(jedisPool);
        }
    }
}
