package rheon.wsd_lora_community.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import rheon.wsd_lora_community.global.queue.RedisMessageSubscriber;

/**
 * Redis 설정
 * - 작업 큐, 세션, 캐시, 레이트 리밋 용도
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    /**
     * Redis 연결 팩토리 (Lettuce 사용, SSL 지원)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        // SSL 설정 (ssl.enabled=true일 때만 활성화)
        LettuceClientConfiguration clientConfig;
        if (sslEnabled) {
            clientConfig = LettuceClientConfiguration.builder()
                    .useSsl()
                    .build();
            log.info("✅ Redis connection configured (SSL enabled): {}:{}", redisHost, redisPort);
        } else {
            clientConfig = LettuceClientConfiguration.defaultConfiguration();
            log.info("✅ Redis connection configured (SSL disabled): {}:{}", redisHost, redisPort);
        }

        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * StringRedisTemplate (키-값 모두 String)
     * - 작업 큐용 (List, Sorted Set)
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * RedisTemplate (객체 직렬화)
     * - Hash 작업 상태 저장용
     * - 세션, 캐시용
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // ObjectMapper 설정 (LocalDateTime 등 Java 8 Time API 지원)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key는 String, Value는 JSON
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis Pub/Sub 메시지 리스너 컨테이너 (동적 관리)
     * - 작업 진행 중일 때만 활성화 (Redis 읽기 비용 절감)
     * - DynamicRedisPubSubService에서 수동으로 시작/중지
     * - 리스너를 추가하지 않고 생성만 하므로 자동 시작되지 않음
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 리스너를 추가하지 않으면 자동으로 시작되지 않음
        // DynamicRedisPubSubService에서 addMessageListener() 호출 시 start() 호출

        log.info("✅ Redis Pub/Sub listener container created (inactive by default, cost optimization)");

        return container;
    }
}
