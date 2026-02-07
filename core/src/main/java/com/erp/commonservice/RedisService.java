package com.erp.commonservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.erp.util.DataUtil.*;

@Component
@RequiredArgsConstructor
public class RedisService {


    private final JedisPool jedisPool;
    private final ObjectMapper mapper = new ObjectMapper();

    public <T> void saveWithTTL(String key, T value, Integer ttlSeconds) {
        if (isNull(ttlSeconds)) {
            saveWithTTL(key, value);
            return;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonValue = objectToJson(value);
            delete(key);
            jedis.setex(key, ttlSeconds, jsonValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void saveWithTTL(String key, T value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonValue = objectToJson(value);
            delete(key);
            jedis.set(key, jsonValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <K, V> void saveWithTTL(String key, Map<K, V> map, Integer ttlSeconds) {
        if (isNull(ttlSeconds)) {
            saveWithTTL(key, map);
            return;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> stringMap = new HashMap<>();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                String strKey = String.valueOf(entry.getKey());
                String strValue = objectToJson(entry.getValue());
                stringMap.put(strKey, strValue);
            }
            delete(key);
            jedis.hset(key, stringMap);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save map into Redis", e);
        }
    }

    public <K, V> void saveWithTTL(String key, Map<K, V> map) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> stringMap = new HashMap<>();

            for (Map.Entry<K, V> entry : map.entrySet()) {
                String strKey = String.valueOf(entry.getKey());
                String strValue = objectToJson(entry.getValue());
                stringMap.put(strKey, strValue);
            }
            delete(key);
            jedis.hset(key, stringMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save map into Redis", e);
        }
    }

    public <V> Map<String, V> getMap(String key, TypeReference<V> valueTypeRef) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> redisMap = jedis.hgetAll(key);
            Map<String, V> resultMap = new HashMap<>();

            for (Map.Entry<String, String> entry : redisMap.entrySet()) {
                V value = mapper.readValue(entry.getValue(), valueTypeRef);
                resultMap.put(entry.getKey(), value);
            }

            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize map from Redis", e);
        }
    }

    public <T> List<T> getObjectList(String key, Class<T> clazz) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(key);
            if (json == null) return Collections.emptyList();
            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public void setTTL(String key, Integer ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(key, ttlSeconds);
        }
    }

    public <T> T getObject(String key, Class<T> clazz) {
        try (Jedis jedis = jedisPool.getResource()) {
            String object = jedis.get(key);
            return objectToClass(object, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
