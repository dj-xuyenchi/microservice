package com.gatewayservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatewayservice.dto.RoleUriDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
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

    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, clazz);
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Mono<Void> saveWithTTL(String key, T value, Integer ttlSeconds) {
        try {
            String jsonValue = mapper.writeValueAsString(value);

            if (ttlSeconds == null) {
                return redisTemplate.opsForValue()
                        .set(key, jsonValue)
                        .then();
            }

            return redisTemplate.opsForValue()
                    .set(key, jsonValue, Duration.ofSeconds(ttlSeconds))
                    .then();

        } catch (Exception e) {
            return Mono.error(e);
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


    public <K, V> Mono<Void> saveMap(String key, Map<K, V> map, Integer ttlSeconds) {
        try {
            Map<String, String> stringMap = new HashMap<>();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                stringMap.put(
                        String.valueOf(entry.getKey()),
                        mapper.writeValueAsString(entry.getValue())
                );
            }

            Mono<Boolean> save = redisTemplate.opsForHash()
                    .putAll(key, stringMap);

            if (ttlSeconds == null) {
                return save.then();
            }

            return save.then(
                    redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds))
            ).then();

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public <V> Mono<Map<String, V>> getMap(
            String key,
            TypeReference<Map<String, V>> typeRef
    ) {
        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> {
                    try {
                        Map<String, V> result =
                                mapper.readValue(json, typeRef);
                        return Mono.just(result);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .defaultIfEmpty(Map.of());
    }

    public <T> Mono<Map<String, T>> getHash(
            String key,
            TypeReference<T> valueType
    ) {
        return redisTemplate.opsForHash()
                .entries(key)
                .collectMap(
                        e -> (String) e.getKey(),
                        e -> {
                            try {
                                return mapper.readValue((String) e.getValue(), valueType);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                );
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
