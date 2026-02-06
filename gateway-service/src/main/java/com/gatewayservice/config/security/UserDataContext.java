package com.gatewayservice.config.security;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDataContext {
    private Long userId;
    private String userName;
    private String sessionId;
    private String ip;
    private String os;
    private List<String> roles;
}
