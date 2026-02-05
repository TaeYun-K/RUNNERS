package com.runners.app.notification.config;

import com.runners.app.notification.listener.NotificationStreamMessageListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.net.InetAddress;
import java.time.Duration;

/**
 * Redis Stream 리스너 컨테이너 설정
 * 전용 리스너 컨테이너로 실시간 이벤트 수신
 */
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
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(connectionFactory, options);

        // Consumer Group에서 읽기
        StreamOffset<String> streamOffset = StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed());
        Consumer consumer = Consumer.from(CONSUMER_GROUP, CONSUMER_NAME);

        container.receive(consumer, streamOffset, messageListener);

        return container;
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
