package com.gatewayservice.config.security;

import com.erp.constant.Constant;
import com.erp.model.CatApi;
import com.erp.vo.BaseRequest;
import com.erp.model.CatSystem;
import com.erp.model.TraceMode;
import com.gatewayservice.config.RedisGateWayService;
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
import reactor.core.scheduler.Schedulers;

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
    private final RedisGateWayService redisGateWayService;
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
        long start = java.lang.System.currentTimeMillis();
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
            claims = Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userName = claims.getSubject();
        String id = claims.getId();
        List<String> roleUser = userService.getUserRole(userName);

        // Kiểm tra danh sách API không cần xác thực
        return redisGateWayService
                .getObjectList(WHITE_LIST_API, CatApi.class)
                .flatMap(whiteList -> {

                    // Nếu Redis chưa có -> load lại
                    if (whiteList.isEmpty()) {
                        return roleService.getApiAndListRoleActiveAndWhiteListApi()
                                .then(redisGateWayService.getObjectList(WHITE_LIST_API, CatApi.class));
                    }

                    return Mono.just(whiteList);
                })
                .flatMap(whiteList -> {

                    // Nếu API nằm trong whitelist
                    if (whiteList.stream().anyMatch(catApi -> catApi.getUri().equals(uri))) {
                        log.info("//API-WHITE-END-POINT -> {}", uri);

                        Authentication auth =
                                new UsernamePasswordAuthenticationToken(userName, null, List.of());

                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)).then(Mono.empty());
                    }

                    return redisGateWayService.getSystemRole();
                })
                // ==== 2. CHECK ROLE API ====
                .flatMap(apiRole -> {

                    List<RoleUriDTO> roles = apiRole.getOrDefault(uri + "_" + service, List.of());

                    if (roles.isEmpty()) {
                        log.warn("//API-PERMISSIONS NOT CONFIGURED -> {}", uri);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    boolean isHasRole = checkRoleApi(roles);
                    if (!isHasRole) {
                        log.warn("//AUTH-SERVICE-FILTER -> Người dùng chưa được phân quyền {}", uri);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    // ==== 3. BUILD USER CONTEXT ====
                    UserDataContext userDataContext = UserDataContext.builder()
                            .userName(userName)
                            .userId(Long.parseLong(id))
                            .roles(roleUser)
                            .sessionId(claims.get("sessionId").toString())
                            .build();

                    log.info("//USER-DATA -> {}", objectToXml(userDataContext));

                    Authentication auth =
                            new UsernamePasswordAuthenticationToken(userDataContext, null, List.of());

                    // ==== 4. CONTINUE FILTER ====
                    return CachedBodyServerHttpRequestDecorator.wrap(exchange)
                            .flatMap(decoratedRequest -> {

                                String requestBody = decoratedRequest.getCachedBodyAsString();
                                log.info("//REQUEST-BODY -> {}", requestBody);

                                return validateRequest(detailUri, requestBody, decoratedRequest)
                                        // ==== VALID OK → CONTINUE ====
                                        .then(Mono.defer(() -> {

                                            ServerHttpResponseDecorator decoratedResponse =
                                                    HttpMethod.GET.name().equals(decoratedRequest.getMethod().name())
                                                            ? getServerHttpResponseDecoratorGet(exchange, userDataContext, detailUri, start)
                                                            : getServerHttpResponseDecorator(exchange, userDataContext, detailUri, start);

                                            ServerWebExchange mutatedExchange = exchange.mutate()
                                                    .request(decoratedRequest)
                                                    .response(decoratedResponse)
                                                    .build();

                                            return chain.filter(mutatedExchange)
                                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                                        }))
                                        // ==== VALID FAIL ====
                                        .onErrorResume(ValidationException.class, e ->
                                                buildErrorResponse(
                                                        exchange,
                                                        e.getMessage(),
                                                        userDataContext,
                                                        detailUri,
                                                        start,
                                                        requestBody
                                                )
                                        )
                                        .onErrorResume(Exception.class, e ->
                                                buildErrorResponse(
                                                        exchange,
                                                        "Lỗi không xác định!",
                                                        userDataContext,
                                                        detailUri,
                                                        start,
                                                        requestBody
                                                )
                                        );
                            });
                });

    }

    private Mono<Void> buildErrorResponse(ServerWebExchange exchange,
                                          String message,
                                          UserDataContext userDataContext,
                                          String[] detailUri,
                                          long start,
                                          String requestBody) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonError = String.format("{\"error\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory()
                .wrap(jsonError.getBytes(StandardCharsets.UTF_8));

        Mono.fromRunnable(() -> {
            try {
                cacheActionUser(userDataContext, detailUri, message, start, requestBody, true, exchange);
            } catch (Exception e) {
                log.error("//LOGACTION-FAILED", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();

        return response.writeWith(Mono.just(buffer));
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

    private Mono<Void> cacheActionUser(
            UserDataContext userDataContext,
            String[] detailUri,
            String result,
            long start,
            String requestBody,
            boolean isError,
            ServerWebExchange exchange
    ) {
        return redisGateWayService.getObjectList(API_URI, CatApi.class)
                .flatMap(apiList -> {

                    CatApi thisCatApi = apiList.stream()
                            .filter(catApi -> catApi.getUri().equals(detailUri[1]))
                            .findFirst()
                            .orElse(null);

                    if (thisCatApi == null) {
                        log.error("//LoiKhongTimThayAPI -> {}", detailUri[1]);
                        return Mono.empty();
                    }

                    long end = java.lang.System.currentTimeMillis();

                    TraceMode trace = TraceMode.builder()
                            .createdAt(new Date())
                            .uri(detailUri[1])
                            .method(thisCatApi.getMethod())
                            .apiId(thisCatApi.getApiId())
                            .systemId(thisCatApi.getSystemId())
                            .action(thisCatApi.getApiCode())
                            .userId(userDataContext.getUserId())
                            .userName(userDataContext.getUserName())
                            .sessionId(userDataContext.getSessionId())
                            .result(result)
                            .rolesActionMoment(String.join(",", userDataContext.getRoles()))
                            .isError(isError)
                            .milliTimeCost(end - start)
                            .build();

                    if (HttpMethod.POST.name().equals(thisCatApi.getMethod()) && requestBody != null) {
                        BaseRequest body = gson.fromJson(requestBody, BaseRequest.class);
                        trace.setIp(body.getIp());
                        trace.setOs(body.getOs());
                        trace.setBrowser(body.getBrowser());
                        trace.setObjectParams(requestBody);
                    }

                    if (HttpMethod.GET.name().equals(thisCatApi.getMethod())) {
                        MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
                        trace.setIp(params.getFirst("ip"));
                        trace.setOs(params.getFirst("os"));
                        trace.setBrowser(params.getFirst("browser"));
                        trace.setObjectParams(objectToJsonNoException(params));
                    }

                    return traceModeServiceImpl.logTrace(trace); // Mono<Void>
                })
                .onErrorResume(e -> {
                    log.error("//TRACE-LOG-FAILED", e);
                    return Mono.empty();
                });
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
            if (Constant.ConstType.NO_APPLY_EFFECT.equals(role.getRoleEffectiveType())
                    && Constant.ConstType.NO_APPLY_EFFECT.equals(role.getApplyType())) {
                return true;
            }
            Date rightNow = new Date();
            // Kiểm tra thời gian hiệu lực
            if (Constant.ConstType.APPLY_EFFECT.equals(role.getRoleEffectiveType())) {
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

    private Mono<Void> validateRequest(
            String[] detailUri,
            String requestBody,
            ServerHttpRequest request
    ) {
        return Mono.zip(
                redisGateWayService.getObjectList(API_URI, CatApi.class),
                redisGateWayService.getObjectList(SYSTEM_SERVICE, CatSystem.class)
        ).flatMap(tuple -> {

            List<CatApi> catApiList = tuple.getT1();
            List<CatSystem> services = tuple.getT2();

            CatSystem service = services.stream()
                    .filter(s -> s.getSystemUriGateway().equals(detailUri[0]))
                    .findFirst()
                    .orElse(null);

            if (service == null) {
                return Mono.error(new ValidationException("Service không tồn tại"));
            }

            CatApi thisCatApi = catApiList.stream()
                    .filter(catApi ->
                            catApi.getUri().equals(detailUri[1]) &&
                                    catApi.getSystemId().equals(service.getSystemId())
                    )
                    .findFirst()
                    .orElse(null);

            if (thisCatApi == null) {
                return Mono.error(new ValidationException("API không tồn tại!"));
            }

            if (!thisCatApi.getMethod().equals(request.getMethod().name())) {
                return Mono.error(new ValidationException("Phương thức API không khớp với cấu hình!"));
            }

            // ===== VALIDATE GET =====
            if (HttpMethod.GET.name().equals(thisCatApi.getMethod())) {
                String queryString = request.getURI().getQuery();
                if (isNullOrEmpty(queryString)) {
                    return Mono.error(new ValidationException("Thiếu tham số yêu cầu!"));
                }

                Map<String, String> params = Arrays.stream(queryString.split("&"))
                        .map(s -> s.split("=", 2))
                        .filter(pair -> pair.length == 2)
                        .collect(Collectors.toMap(
                                pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                                pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                        ));

                if (isNullOrEmpty(params.get("ip"))
                        || isNullOrEmpty(params.get("os"))
                        || isNullOrEmpty(params.get("browser"))) {
                    return Mono.error(new ValidationException("Thiếu tham số yêu cầu!"));
                }
            }

            // ===== VALIDATE POST =====
            if (HttpMethod.POST.name().equals(thisCatApi.getMethod())) {
                BaseRequest bodyObj = gson.fromJson(requestBody, BaseRequest.class);
                if (isNullOrEmpty(bodyObj.getIp())
                        || isNullOrEmpty(bodyObj.getOs())
                        || isNullOrEmpty(bodyObj.getBrowser())) {
                    return Mono.error(new ValidationException("Thiếu tham số yêu cầu!"));
                }
            }

            return Mono.empty(); // ✅ validate OK
        });
    }

}
