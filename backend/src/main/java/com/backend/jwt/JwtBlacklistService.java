package com.backend.jwt;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;

    public JwtBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expirationTimeMillis) {
        // Redis TTL (Time To Live) necesita segundos, expirationTimeMillis está en
        // milisegundos
        long ttl = expirationTimeMillis / 1000;
        if (ttl > 0) {
            // Almacenar el token en Redis con un TTL
            // La clave será "jwt:blacklist:<token>" y el valor puede ser un simple
            // "blacklisted"
            redisTemplate.opsForValue().set("jwt:blacklist:" + token, "blacklisted", ttl, TimeUnit.SECONDS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("jwt:blacklist:" + token));
    }
}
