package com.erp.authenservice.dto.request;

import com.erp.vo.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest extends BaseRequest {
    private String userName;
    private String password;
}
