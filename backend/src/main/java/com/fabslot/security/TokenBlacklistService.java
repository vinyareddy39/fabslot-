package com.fabslot.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expirationTimeMs) {
        if (token != null && !token.isBlank()) {
            cleanExpiredTokens();
            blacklistedTokens.put(token, expirationTimeMs);
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || !blacklistedTokens.containsKey(token)) {
            return false;
        }

        Long expiry = blacklistedTokens.get(token);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            blacklistedTokens.remove(token);
            return false;
        }

        return true;
    }

    private void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() != null && entry.getValue() < now);
    }
}
