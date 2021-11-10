package com.jonssonyan.service.impl;

import com.jonssonyan.limiter.RateLimiter;
import com.jonssonyan.limiter.RateLimiterManager;
import com.jonssonyan.service.LimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LimiterServiceImpl implements LimiterService {
    private final RateLimiter rateLimiter;

    public LimiterServiceImpl(RateLimiterManager rateLimiterManager) {
        // 创建一个流控器
        this.rateLimiter = rateLimiterManager.createIfAbsent(5, 10, "rateLimiterTest");
    }

    @Override
    public String rateLimiterTest() {
        // 开启限流
        rateLimiter.acquire();
        return "success";
    }
}
