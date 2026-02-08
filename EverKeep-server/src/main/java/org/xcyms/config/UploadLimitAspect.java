package org.xcyms.config;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.xcyms.common.annotation.UploadLimit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 上传限流aop
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月21日 17:22
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class UploadLimitAspect {

    private final StringRedisTemplate redisTemplate;

    @Before("@annotation(uploadLimit)")
    public void doBefore(JoinPoint joinPoint, UploadLimit uploadLimit) {
        // 1. 获取当前用户ID
        long userId = StpUtil.getLoginIdAsLong();
        // 忽略管理员
        if (userId == 1L) {
            return;
        }

        // 2. 检查小时限制
        String hourKey = "upload_limit:" + userId + ":hour:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        checkLimit(hourKey, uploadLimit.hour(), 3600, "小时");

        // 3. 检查天限制
        String dayKey = "upload_limit:" + userId + ":day:" + LocalDate.now();
        checkLimit(dayKey, uploadLimit.day(), 86400, "天");

        // 4. 检查月限制
        String monthKey = "upload_limit:" + userId + ":month:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        checkLimit(monthKey, uploadLimit.month(), 2592000, "月");
    }

    private void checkLimit(String key, int limit, long expireSeconds, String unitName) {
        long current = redisTemplate.opsForValue().increment(key);

        if (current == 1) {
            redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
        }

        if (current > limit) {
            redisTemplate.opsForValue().decrement(key); // 回退计数器
            log.warn("用户上传触发{}限流: key={}, limit={}", unitName, key, limit);
            throw new RuntimeException("已达到每" + unitName + "上传上限(" + limit + "次)，请稍后再试");
        }
    }
}