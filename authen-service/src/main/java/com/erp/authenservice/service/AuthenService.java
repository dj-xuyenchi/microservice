package com.erp.authenservice.service;

import com.erp.authenservice.dto.response.BtnRoleResponse;
import com.erp.model.CatApi;

import java.util.List;

public interface AuthenService {
    BtnRoleResponse getBtnRoleForUser();

    List<CatApi> testTai();
}
