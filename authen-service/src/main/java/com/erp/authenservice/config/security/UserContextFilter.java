package com.erp.authenservice.config.security;

import com.erp.constant.Constant;
import com.erp.exception.AppException;
import com.erp.vo.UserDataContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

import static com.erp.util.DataUtil.isNullOrEmpty;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserContextFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws AppException {
        try {
            String userId = request.getHeader(Constant.FilterGatewayParams.X_USER_ID);
            String userName = request.getHeader(Constant.FilterGatewayParams.X_USER_NAME);
            String roles = request.getHeader(Constant.FilterGatewayParams.X_USER_ROLES);

            log.info("//USER-ID -> {}", userId);
            log.info("//USER-NAME -> {}", userName);
            log.info("//USER-ROLES -> {}", roles);
            if (userId != null) {
                String rolesStr = isNullOrEmpty(roles) ? "" : roles;
                UserDataContext userDataContext = UserDataContext.builder()
                        .userName(userName)
                        .userId(Long.parseLong(userId))
                        .roles(List.of(rolesStr.split(",")))
                        .build();

                Authentication auth = new UsernamePasswordAuthenticationToken(userDataContext, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("//ERROR -> {}", ExceptionUtils.getStackTrace(e));
            if (e.getMessage().contains("exeption.AppException")) {
                throw (AppException) e;
            }
        }
    }
}
