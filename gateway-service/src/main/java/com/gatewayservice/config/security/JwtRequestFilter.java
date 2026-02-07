package com.gatewayservice.config.security;

import com.erp.commonservice.RedisService;
import com.erp.constant.Constant;
import com.erp.model.ApiUri;
import com.erp.model.BaseRequest;
import com.erp.model.SystemApplication;
import com.erp.model.TraceMode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.gatewayservice.dto.RoleUriDTO;
import com.gatewayservice.service.IRoleService;
import com.gatewayservice.service.IUserService;
import com.gatewayservice.service.impl.TraceModeServiceImpl;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.ValidationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.erp.util.DataUtil.*;
import static com.erp.util.DateUtil.isBetween;
import static com.gatewayservice.constant.RequestGatewayApi.*;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter implements WebFilter {

    private final TraceModeServiceImpl traceModeServiceImpl;
    private final Gson gson;
    @Value("${jwt.secret}")
    private String secret;
    private final RedisService redisService;
    private final IUserService userService;
    private final IRoleService roleService;

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        if (HttpMethod.OPTIONS.matches(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }
        log.info("//GATE-WAY Listen URI -> {}", exchange.getRequest().getURI());
        log.info("//GATE-WAY Listen METHOD -> {}", exchange.getRequest().getMethod().name());
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        String[] detailUri = getDetailServiceAndUri(request.getURI().getPath());
        String service = detailUri[0];
        String uri = detailUri[1];
        log.info("//REQUEST INFOR -> {} {}", service, uri);
        // Bypass login/register/swagger
        if (uri.contains("/swagger-ui") || uri.contains("/v3")
            || uri.equals("/login") || uri.equals("/register")) {
            Authentication auth = new UsernamePasswordAuthenticationToken("NO-Secure", null, List.of());
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (isNullOrEmpty(authHeader) || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userName = claims.getSubject();
        String id = claims.getId();
        List<String> roleUser = userService.getUserRole(Long.parseLong(id));

        // Kiểm tra danh sách API không cần xác thực
        List<ApiUri> whiteListEndpoint = redisService.getObjectList(WHITE_LIST_API, ApiUri.class);
        if (isNullOrEmpty(whiteListEndpoint)) {
            roleService.getApiAndListRoleActiveAndWhiteListApi();
            whiteListEndpoint = redisService.getObjectList(WHITE_LIST_API, ApiUri.class);
        }

        if (whiteListEndpoint.stream().anyMatch(apiUri -> apiUri.getUri().equals(uri))) {
            log.info("//API-WHITE-END-POINT -> {}", uri);
            Authentication auth = new UsernamePasswordAuthenticationToken(userName, null, List.of());
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        }

        // role check
        Map<String, List<RoleUriDTO>> apiRole = redisService.getMap(SYSTEM_ROLE, new TypeReference<>() {
        });
        List<RoleUriDTO> roles = apiRole.getOrDefault(uri, List.of());
        if (isNullOrEmpty(roles)) {
            log.warn("//API-PERMISSIONS NOT CONFIGURED -> {}", uri);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Kiểm tra xem role người dùng có role nào trùng với role đang active không
        boolean isHasRole = checkRoleApi(roles);

        if (!isHasRole) {
            log.warn("//AUTH-SERVICE-FILTER -> {}", "Người dùng chưa được phân quyền vào API này " + uri);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        // Tạo đối tượng lưu dữ liệu user để sử dụng ở các layer khác trong phiên api
        UserDataContext userDataContext = UserDataContext.builder()
                .userName(userName)
                .userId(Long.parseLong(id))
                .roles(roleUser)
                .sessionId(claims.get("sessionId").toString())
                .build();
        log.info("//USER-DATA -> {}", objectToXml(userDataContext));

        Authentication auth = new UsernamePasswordAuthenticationToken(userDataContext, null, List.of());


        return CachedBodyServerHttpRequestDecorator.wrap(exchange)
                .flatMap(decoratedRequest -> {
                    String requestBody = decoratedRequest.getCachedBodyAsString();
                    log.info("//REQUEST-BODY -> {}", decoratedRequest.getCachedBodyAsString());

                    try {
                        // Validate request trước khi gọi API
                        validateRequest(detailUri, requestBody, decoratedRequest);
                    } catch (ValidationException e) {
                        log.warn("//API validation failed -> {}", ExceptionUtils.getStackTrace(e));

                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.BAD_REQUEST);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                        String jsonError = String.format("{\"error\":\"%s\"}", e.getMessage());
                        DataBuffer buffer = response.bufferFactory().wrap(jsonError.getBytes(StandardCharsets.UTF_8));
                        try {
                            cacheActionUser(userDataContext, detailUri, ExceptionUtils.getStackTrace(e), start, requestBody, true, exchange);
                        } catch (Exception ex) {
                            log.error("//LOGACTION-FAILE -> {}", ExceptionUtils.getStackTrace(ex));
                        }
                        return response.writeWith(Mono.just(buffer));
                    } catch (Exception e) {
                        log.error("//Unexpected error when validating request", ExceptionUtils.getStackTrace(e));
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.BAD_REQUEST);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                        String jsonError = String.format("{\"error\":\"Lỗi không xác định!\"}");
                        DataBuffer buffer = response.bufferFactory().wrap(jsonError.getBytes(StandardCharsets.UTF_8));
                        try {
                            cacheActionUser(userDataContext, detailUri, ExceptionUtils.getStackTrace(e), start, requestBody, true, exchange);
                        } catch (Exception ex) {
                            log.error("//LOGACTION-FAILE -> {}", ExceptionUtils.getStackTrace(ex));
                        }
                        return response.writeWith(Mono.just(buffer));
                    }

                    ServerHttpResponseDecorator decoratedResponse;
                    if ("GET".equals(decoratedRequest.getMethod().name())) {
                        decoratedResponse = getServerHttpResponseDecoratorGet(exchange, userDataContext, detailUri, start);
                    } else {
                        decoratedResponse = getServerHttpResponseDecorator(exchange, userDataContext, detailUri, start);
                    }

                    // tạo lại exchange với request + response mới
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(decoratedRequest)
                            .response(decoratedResponse)
                            .build();

                    return chain.filter(mutatedExchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                });

    }

    private ServerHttpResponseDecorator getServerHttpResponseDecoratorGet(ServerWebExchange exchange,
                                                                          UserDataContext userDataContext,
                                                                          String[] detailUri, long start) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    return Flux.from(body)
                            .collectList()
                            .flatMap(dataBuffers -> {
                                // Tính tổng size
                                int totalSize = dataBuffers.stream()
                                        .mapToInt(DataBuffer::readableByteCount)
                                        .sum();
                                byte[] allBytes = new byte[totalSize];
                                int offset = 0;

                                // Copy từng DataBuffer vào allBytes
                                for (DataBuffer db : dataBuffers) {
                                    ByteBuffer bb = db.asByteBuffer();
                                    int count = bb.remaining();
                                    bb.get(allBytes, offset, count);
                                    offset += count;
                                    DataBufferUtils.release(db); // giải phóng buffer
                                }
                                String responseBody = new String(allBytes, StandardCharsets.UTF_8);

                                // Cache action user
                                try {
                                    String requestBody = objectToJson(exchange.getRequest().getQueryParams());
                                    cacheActionUser(userDataContext, detailUri, responseBody, start, requestBody, false, exchange);
                                } catch (Exception e) {
                                    log.error("//LoiKoLuuDuocTraceMode -> {}", ExceptionUtils.getStackTrace(e));
                                }

                                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(allBytes);
                                return super.writeWith(Mono.just(buffer));
                            });
                }
                return super.writeWith(body);
            }
        };
    }

    private ServerHttpResponseDecorator getServerHttpResponseDecorator(
            ServerWebExchange exchange,
            UserDataContext userDataContext,
            String[] detailUri, long start) {

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        return new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    return Flux.from(body)
                            .collectList()
                            .flatMap(dataBuffers -> {
                                // Gộp tất cả DataBuffer lại thành 1 mảng byte
                                int totalSize = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
                                byte[] allBytes = new byte[totalSize];
                                int offset = 0;
                                for (DataBuffer db : dataBuffers) {
                                    int count = db.readableByteCount();
                                    db.read(allBytes, offset, count);
                                    offset += count;
                                    DataBufferUtils.release(db); // quan trọng: tránh memory leak
                                }

                                String responseBody = new String(allBytes, StandardCharsets.UTF_8);
                                log.info("Response body: {}", responseBody);
                                String requestBody = null;

                                if (exchange.getRequest() instanceof CachedBodyServerHttpRequestDecorator cachedReq) {
                                    requestBody = cachedReq.getCachedBodyAsString();
                                }
                                // Cache/log thêm action user
                                try {
                                    cacheActionUser(userDataContext, detailUri, responseBody, start, requestBody, false, exchange);
                                } catch (Exception e) {
                                    log.error("//LoiKoLuuDuocTraceMode -> {}", ExceptionUtils.getStackTrace(e));
                                }
                                // Nếu muốn sửa nội dung response thì modify ở đây
                                byte[] newContent = responseBody.getBytes(StandardCharsets.UTF_8);

                                DataBuffer newBuffer = bufferFactory.wrap(newContent);
                                return super.writeWith(Mono.just(newBuffer));
                            });
                }
                return super.writeWith(body);
            }
        };
    }

    private void cacheActionUser(UserDataContext userDataContext, String[] detailUri, String result, long start, String requestBody, boolean isError, ServerWebExchange exchange) throws Exception {

        List<ApiUri> apiList = redisService.getObjectList(API_URI, ApiUri.class);
        ApiUri thisApi = apiList.stream()
                .filter(api -> {
                    return api.getUri().equals(detailUri[1]);
                })
                .findFirst().orElse(null);
        if (thisApi == null) {
            log.error("//LoiKhongTimThayAPI -> {}", detailUri[1]);
            return;
        }
        long end = System.currentTimeMillis();
        TraceMode trace = TraceMode.builder()
                .createdAt(new Date())
                .uri(detailUri[1])
                .method(thisApi.getMethod())
                .apiId(thisApi.getApiUriId())
                .appId(thisApi.getApplicationId())
                .action(thisApi.getAction())
                .userId(userDataContext.getUserId())
                .userName(userDataContext.getUserName())
                .sessionId(userDataContext.getSessionId())
                .result(result)
                .rolesActionMoment(String.join(",", userDataContext.getRoles()))
                .isError(isError)
                .milliTimeCost(end - start)
                .build();
        if ("POST".equals(thisApi.getMethod())) {
            BaseRequest requestBodyOb = gson.fromJson(requestBody, BaseRequest.class);
            trace.setIp(requestBodyOb.getIp());
            trace.setOs(requestBodyOb.getOs());
            trace.setBrowser(requestBodyOb.getBrowser());
            trace.setObjectParams(requestBody);
        }
        if ("GET".equals(thisApi.getMethod())) {
            MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
            String ip = params.getFirst("ip");
            String os = params.getFirst("os");
            String browser = params.getFirst("browser");
            trace.setIp(ip);
            trace.setOs(os);
            trace.setBrowser(browser);
            trace.setObjectParams(objectToJson(params));
        }
        traceModeServiceImpl.logTrace(trace);
    }

    // Lấy tên service và uri bỏ query string
    private String[] getDetailServiceAndUri(String uri) {
        // Bỏ query string nếu có
        String path = uri.split("\\?")[0];

        // Cắt theo dấu "/"
        String[] parts = path.split("/");

        // Vì chuỗi bắt đầu bằng "/", nên phần tử đầu tiên sẽ rỗng
        // => service = parts[1], uri = parts[2]
        if (parts.length >= 3) {
            return new String[]{parts[1], "/" + parts[2]};
        }

        return new String[]{"", "/"};
    }

    private boolean checkRoleApi(List<RoleUriDTO> roles) {
        for (RoleUriDTO role : roles) {
            // Nếu cả role và phân quyền của role với api đều là loại không yêu cầu phạm vi thời gian hiệu lực!
            if (Constant.ConstType.NO_APPLY_EFFECT.equals(role.getRoleType()) && Constant.ConstType.NO_APPLY_EFFECT.equals(role.getApplyType())) {
                return true;
            }
            Date rightNow = new Date();
            // Kiểm tra thời gian hiệu lực
            if (Constant.ConstType.APPLY_EFFECT.equals(role.getRoleType())) {
                Date roleFrom = role.getRoleFrom();
                Date roleTo = role.getRoleTo();
                if (isBetween(roleFrom, roleTo, rightNow)) {
                    return true;
                }
            }
            if (Constant.ConstType.APPLY_EFFECT.equals(role.getApplyType())) {
                Date applyFrom = role.getApplyFrom();
                Date applyTo = role.getApplyTo();
                if (isBetween(applyFrom, applyTo, rightNow)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateRequest(String[] detailUri, String requestBody, ServerHttpRequest request) {
        List<ApiUri> apiList = redisService.getObjectList(API_URI, ApiUri.class);
        List<SystemApplication> services = redisService.getObjectList(SYSTEM_SERVICE, SystemApplication.class);
        SystemApplication service = services.stream().filter(x->{
            return x.getServiceUriGateway().equals(detailUri[0]);
        }).findFirst().orElse(null);

        if(service == null) {
            throw new ValidationException("Service không tồn tại");
        }

        ApiUri thisApi = apiList.stream()
                .filter(api -> {
                    return api.getUri().equals(detailUri[1]) && api.getApplicationId().equals(service.getApplicationId());
                })
                .findFirst().orElse(null);

        if (thisApi == null) {
            throw new ValidationException("API không tồn tại!");
        }

        if (!thisApi.getMethod().equals(request.getMethod().name())) {
            throw new ValidationException("Phương thức API không khớp với cấu hình!");
        }

        if ("GET".equals(thisApi.getMethod())) {
            String queryString = request.getURI().getQuery();
            if (isNullOrEmpty(queryString)) {
                throw new ValidationException("Thiếu tham số yêu cầu!");
            }
            Map<String, String> params = Arrays.stream(queryString.split("&"))
                    .map(s -> s.split("=", 2))
                    .filter(pair -> pair.length == 2)
                    .collect(Collectors.toMap(
                            pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                            pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                    ));

            if (isNullOrEmpty(params.get("ip")) || isNullOrEmpty(params.get("os")) || isNullOrEmpty(params.get("browser"))) {
                throw new ValidationException("Thiếu tham số yêu cầu!");
            }
        }

        if ("POST".equals(thisApi.getMethod())) {
            BaseRequest bodyObj = gson.fromJson(requestBody, BaseRequest.class);
            if (isNullOrEmpty(bodyObj.getIp()) || isNullOrEmpty(bodyObj.getOs()) || isNullOrEmpty(bodyObj.getBrowser())) {
                throw new ValidationException("Thiếu tham số yêu cầu!");
            }
        }
    }

}
