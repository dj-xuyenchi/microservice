package com.erp.authenservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class BcryptConfig {
    @Bean
    public BCryptPasswordEncoder bcrypt() {
        // salt 10 vòng mã hóa phía FE cũng phải 10 tránh lỗi!
        return new BCryptPasswordEncoder(10);
    }
}
