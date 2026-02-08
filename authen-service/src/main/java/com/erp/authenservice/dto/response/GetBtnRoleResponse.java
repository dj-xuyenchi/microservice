package com.erp.authenservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class GetBtnRoleResponse {
    private Map<String, List<String>> btnRole;
}
