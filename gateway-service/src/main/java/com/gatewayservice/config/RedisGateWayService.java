package com.gatewayservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatewayservice.dto.RoleUriDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.gatewayservice.constant.RequestGatewayApi.SYSTEM_ROLE;


@Component
public class RedisGateWayService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public RedisGateWayService(
            @Qualifier("customReactiveRedisTemplate")
            ReactiveRedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Map<String, List<RoleUriDTO>>> getSystemRole() {
        return redisTemplate.opsForValue()
                .get(SYSTEM_ROLE)
                .map(this::jsonToMap)
                .defaultIfEmpty(Map.of());
    }

    private Map<String, List<RoleUriDTO>> jsonToMap(String json) {
        try {
            return mapper.readValue(
                    json,
                    new TypeReference<Map<String, List<RoleUriDTO>>>() {
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse JSON to Map", e);
        }
    }

    public <T> Mono<Void> saveWithTTL(String key, T value) {
        try {
            String jsonValue = mapper.writeValueAsString(value);

            return redisTemplate.opsForValue()
                    .set(key, jsonValue)
                    .then();


        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public <T> Mono<List<T>> getObjectList(String key, Class<T> clazz) {
        return redisTemplate
                .opsForValue()
                .get(key)
                .<List<T>>handle((json, sink) -> {
                    try {
                        JavaType type = mapper.getTypeFactory()
                                .constructCollectionType(List.class, clazz);
                        @SuppressWarnings("unchecked")
                        List<T> list = (List<T>) mapper.readValue(json, type);
                        sink.next(list);
                    } catch (Exception e) {
                        sink.error(new RuntimeException(e));
                    }
                })
                .defaultIfEmpty(Collections.emptyList());
    }

}
