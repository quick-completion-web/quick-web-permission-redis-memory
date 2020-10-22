package tech.guyi.web.quick.permission.memory.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisMemoryAutoConfiguration {

    @Bean
    public RedisAuthorizationInfoMemory redisAuthorizationInfoMemory(){
        return new RedisAuthorizationInfoMemory();
    }

}
