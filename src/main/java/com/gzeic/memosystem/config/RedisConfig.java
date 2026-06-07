package com.gzeic.memosystem.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Redis 缓存配置（Spring Cache）
 *
 * 目标：
 * - 让项目可以使用 @Cacheable / @CacheEvict 等注解
 * - 把缓存值序列化为 JSON（更直观，便于排查）
 *
 * 多租户注意：
 * - “租户隔离”不在这里做，而是在缓存 key 的生成规则里做
 * - 本项目使用 TenantCacheKey 统一生成带 tenant 前缀的 key，避免缓存串租户
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置 RedisCacheManager（Spring Cache 的核心实现）
     *
     * @param connectionFactory Redis 连接工厂（由 spring-boot-starter-data-redis 自动提供）
     * @return RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                ));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}

