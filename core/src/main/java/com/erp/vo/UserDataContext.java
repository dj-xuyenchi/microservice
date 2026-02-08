package com.erp.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class UserDataContext {
    private Long userId;
    private String userName;
    private String sessionId;
    private String ip;
    private String os;
    private List<String> roles;
}
