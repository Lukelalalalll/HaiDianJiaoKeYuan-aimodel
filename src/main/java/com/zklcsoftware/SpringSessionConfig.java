package com.zklcsoftware;

/**
 * @author zhushaog
 * @version 1.0
 * @ClassName SpringSessionConfig.java
 * @Description TODO
 * @createTime 2021/07/07 18:09
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 类名：RedisCacheConfiguration<br>
 * 描述：<br>
 * 创建人：<br>
 * 创建时间：2016/9/6 17:33<br>
 *
 * @version v1.0
 */

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)//在这里设置Session过期时间，单位：秒
@Slf4j
public class SpringSessionConfig {

    /**
     * @Description  springsession是否线程池模式
     * @Author zhushaog
     * @UpdateTime 2021/7/7 18:10
     * @return: org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
     * @throws
     */
    @Bean
    public ThreadPoolTaskExecutor springSessionRedisTaskExecutor() {
        ThreadPoolTaskExecutor springSessionRedisTaskExecutor = new ThreadPoolTaskExecutor();
        springSessionRedisTaskExecutor.setCorePoolSize(15);
        springSessionRedisTaskExecutor.setMaxPoolSize(30);
        springSessionRedisTaskExecutor.setKeepAliveSeconds(10);
        springSessionRedisTaskExecutor.setQueueCapacity(1000);
        springSessionRedisTaskExecutor.setThreadNamePrefix("SESSION_TASK_EXEC");
        return springSessionRedisTaskExecutor;
    }

}
