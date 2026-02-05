package com.runners.app.notification.config;

import com.runners.app.notification.listener.NotificationStreamMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.time.Duration;

/**
 * Redis Stream 리스너 컨테이너 설정
 * 전용 리스너 컨테이너로 실시간 이벤트 수신
 */
@Slf4j
@Configuration
public class NotificationStreamConfig {

    private static final String STREAM_KEY = "notification:events:comment-created";
    private static final String CONSUMER_GROUP = "notification-workers";
    private static final String CONSUMER_NAME = "worker-" + getHostname() + "-" + ProcessHandle.current().pid();

    /**
     * StreamMessageListenerContainer 설정
     * 전용 리스너 컨테이너로 실시간 이벤트 수신
     */
    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
            notificationStreamContainer(
                    @Qualifier("notificationRedisConnectionFactory") RedisConnectionFactory connectionFactory,
                    NotificationStreamMessageListener messageListener
            ) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(5))
                        .errorHandler(t -> log.error("Notification stream listener polling error", t))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(connectionFactory, options);

        ensureConsumerGroupExists(connectionFactory);

        // Consumer Group에서 읽기
        StreamOffset<String> streamOffset = StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed());
        Consumer consumer = Consumer.from(CONSUMER_GROUP, CONSUMER_NAME);

        container.receive(consumer, streamOffset, messageListener);

        container.start();
        log.info("Notification stream container started: stream={}, group={}, consumer={}", STREAM_KEY, CONSUMER_GROUP, CONSUMER_NAME);

        return container;
    }

    private void ensureConsumerGroupExists(RedisConnectionFactory connectionFactory) {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.streamCommands().xGroupCreate(
                    STREAM_KEY.getBytes(StandardCharsets.UTF_8),
                    CONSUMER_GROUP,
                    ReadOffset.from("0-0"),
                    true
            );
            log.info("Notification consumer group ensured (mkstream): stream={}, group={}", STREAM_KEY, CONSUMER_GROUP);
        } catch (Exception e) {
            // BUSYGROUP (already exists) 등은 정상 시나리오라 debug로만 남김
            log.debug("Notification consumer group already exists or cannot be created now: {}", e.getMessage());
        }
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
