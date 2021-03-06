package com.jonssonyan.controller;

import com.jonssonyan.constant.RedisConstant;
import com.jonssonyan.entity.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Redisson分布式锁
 * 参考：https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8
 */
@RestController
@Slf4j
public class TestController {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 可重入锁
     *
     * @return
     */
    @GetMapping("/reentrantLock")
    public Result reentrantLock() {
        RLock lock = redissonClient.getLock("reentrantLock");
        // 最常见的使用方法
        lock.lock();
        // 加锁以后10秒钟自动解锁 无需调用unlock方法手动解锁
        lock.lock(10, TimeUnit.SECONDS);
        try {
            // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
            boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
            if (res) {
                log.info("reentrantLock test");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

    /**
     * 公平锁
     *
     * @return
     */
    @GetMapping("/fairLock")
    public Result fairLock() {
        RLock fairLock = redissonClient.getFairLock("fairLock");
        fairLock.lock();
        // 10秒钟以后自动解锁 无需调用unlock方法手动解锁
        fairLock.lock(10, TimeUnit.SECONDS);

        try {
            // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
            boolean res = fairLock.tryLock(100, 10, TimeUnit.SECONDS);
            if (res) {
                log.info("fairLock test");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            fairLock.unlock();
        }
        return Result.success();
    }

    /**
     * 读锁
     *
     * @return
     */
    @GetMapping("/readLock")
    public Result readLock() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(RedisConstant.READ_LOCK);
        // 读之前加读锁，读锁的作用就是等待该lock key释放写锁以后再读
        RLock rLock = readWriteLock.readLock();
        try {
            // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
            boolean res = rLock.tryLock(100, 10, TimeUnit.SECONDS);
            if (res) {
                String uuid = stringRedisTemplate.opsForValue().get("flag");
                log.info(uuid);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            rLock.unlock();
        }
        return Result.success();
    }

    /**
     * 写锁
     *
     * @return
     */
    @GetMapping("/writeLock")
    public Result writeLock() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(RedisConstant.WRITE_LOCK);
        RLock rLock = readWriteLock.writeLock();
        try {
            // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
            boolean res = rLock.tryLock(100, 10, TimeUnit.SECONDS);
            if (res) {
                stringRedisTemplate.opsForValue().set("flag", "writeLock test");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            rLock.unlock();
        }
        return Result.success();
    }
}
