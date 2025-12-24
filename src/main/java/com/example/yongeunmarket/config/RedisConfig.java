package com.example.yongeunmarket.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.yongeunmarket.service.RedisSubscriber;

@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(host, port);
	}

	//RedisTemplate 사용을 위한 추가
	@Bean
	public RedisTemplate<?, ?> redisTemplate() {

		RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());    //StringRedisTemplate 설정
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	// 채팅용 연결 팩토리
	@Bean
	@Qualifier("chatPubSub")
	public RedisConnectionFactory chatPubSubFactory() {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
		configuration.setHostName(host);
		configuration.setPort(port);
		return new LettuceConnectionFactory(configuration);
	}

	// 채팅용 Publish 템플릿
	@Bean
	@Qualifier("chatPubSub")
	public StringRedisTemplate chatRedisTemplate(
		@Qualifier("chatPubSub") RedisConnectionFactory chatPubSubFactory) {
		// 위에서 만든 'chatPubSubFactory'를 주입받아 사용
		return new StringRedisTemplate(chatPubSubFactory);
	}

	// 채팅용 Subscribe 리스너 컨테이너
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		@Qualifier("chatPubSub") RedisConnectionFactory chatPubSubFactory,
		MessageListenerAdapter messageListenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(chatPubSubFactory); // 채팅용 팩토리 연결
		container.addMessageListener(messageListenerAdapter, new PatternTopic("chatroom"));
		return container;
	}

	// 리스너 어댑터
	@Bean
	public MessageListenerAdapter messageListenerAdapter(RedisSubscriber redisSubscriber) {
		return new MessageListenerAdapter(redisSubscriber, "sendMessage");
	}
}
