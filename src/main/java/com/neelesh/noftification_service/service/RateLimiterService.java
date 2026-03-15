package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.enums.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final StringRedisTemplate redisTemplate;

    private static final Long MAX_RATE = 10L;

    public void checkRateLimit(String userId, Channel channel){
        String key = "limit:" + userId + ":" + channel;
        ValueOperations<String, String> valueOps= redisTemplate.opsForValue();
        Long count = valueOps.increment(key);
        if(count == 1){
            redisTemplate.expire(key, 60, TimeUnit.MINUTES);
        }else if(count>MAX_RATE){
            throw new RateLimitExceededException("Rate limit exceeded for the user: " + userId);
        }
    }

    public static class RateLimitExceededException extends RuntimeException{
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}

