package com.huiyi.medical.config;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, userId);
        return token;
    }

    public Long getUserId(String token) {
        return tokens.get(token);
    }

    public void remove(String token) {
        tokens.remove(token);
    }
}
