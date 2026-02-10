package com.erp.authenservice.controller;

import com.erp.authenservice.dto.request.AuthRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthenController {
    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) throws Exception {
        return ResponseEntity.ok("");
    }

    @GetMapping(value = "/api")
    public ResponseEntity<?> api(AuthRequest authRequest) throws Exception {
        return ResponseEntity.ok("");
    }
}
