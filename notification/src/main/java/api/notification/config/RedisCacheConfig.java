package api.notification.config;

import api.notification.domain.Notification;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class RedisCacheConfig {

    @Bean
    public ReactiveHashOperations<String, String, Notification> hashOperations(ReactiveRedisConnectionFactory redisConnectionFactory){
        var template = new ReactiveRedisTemplate<>(
                redisConnectionFactory,
                RedisSerializationContext.<String, Notification>newSerializationContext(new StringRedisSerializer())
                        .hashKey(new GenericToStringSerializer<>(String.class))
                        .hashValue(new Jackson2JsonRedisSerializer<>(Notification.class))
                        .build()
        );
        return template.opsForHash();
    }
}