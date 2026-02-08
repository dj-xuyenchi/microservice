package com.erp.authenservice;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@RequiredArgsConstructor
public class AuthenServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenServiceApplication.class, args);
    }

}
