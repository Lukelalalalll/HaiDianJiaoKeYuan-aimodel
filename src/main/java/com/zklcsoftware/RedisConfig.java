package com.zklcsoftware;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.UnknownHostException;

/**
 * 
 * @author audin
 *
 */
@Configuration
public class RedisConfig {

	// @Autowired
	// private RedisMessageListenerLiveUsers redisMessageListenerLiveUsers;
	// @Autowired
	// private RedisMessageListenerTotalOnlineCount redisMessageListenerTotalOnlineCount;
	
    // @Autowired
	// private SimpMessagingTemplate messagingTemplate;
	// @Autowired 
	// private RedisTemplate<String, Object> redisTemplate;
	// @Autowired
	// private StringRedisTemplate stringRedisTemplate;
	
	// @Bean
	// public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
	// 	StringRedisTemplate template = new StringRedisTemplate(factory);
	// 	Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
	// 	ObjectMapper om = new ObjectMapper();
	// 	om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	// 	om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	// 	jackson2JsonRedisSerializer.setObjectMapper(om);
	// 	template.setValueSerializer(jackson2JsonRedisSerializer);
	// 	template.afterPropertiesSet();
	// 	return template;
	// }

	@Bean
	@ConditionalOnMissingBean(name = "redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory)
			throws UnknownHostException  {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(factory);

        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer<Object> jacksonSeial = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSeial.setObjectMapper(om);

        // 值采用json序列化
        template.setValueSerializer(jacksonSeial);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());

        // 设置hash key 和value序列化模式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jacksonSeial);
        template.afterPropertiesSet();

        return template;
	}
	
	@Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}
