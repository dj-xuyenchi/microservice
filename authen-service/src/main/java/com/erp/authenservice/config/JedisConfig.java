package com.erp.authenservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class JedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);         // số lượng kết nối tối đa
        poolConfig.setMaxIdle(5);           // số lượng idle connection tối đa
        poolConfig.setMinIdle(1);           // số lượng idle tối thiểu
        poolConfig.setTestOnBorrow(true);   // kiểm tra trước khi mượn connection

        return new JedisPool(poolConfig, host, port, 2000, password);
    }


}
