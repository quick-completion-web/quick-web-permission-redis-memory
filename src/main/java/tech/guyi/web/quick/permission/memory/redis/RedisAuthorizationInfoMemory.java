package tech.guyi.web.quick.permission.memory.redis;

import com.google.gson.Gson;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import tech.guyi.web.quick.permission.authorization.AuthorizationInfo;
import tech.guyi.web.quick.permission.authorization.memory.AuthorizationInfoMemory;
import tech.guyi.web.quick.permission.configuration.PermissionConfiguration;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
class AuthorizationRedisEntry {
    private String json;
    private String classes;
}

public class RedisAuthorizationInfoMemory implements AuthorizationInfoMemory {

    private final Gson gson = new Gson();

    @Resource
    private RedisTemplate<String,String> template;
    @Resource
    private PermissionConfiguration configuration;

    private String getKey(String key){
        return "Authorization/" + key;
    }

    @Override
    public String forType() {
        return "redis";
    }

    @Override
    public boolean contains(String key) {
        return Optional.ofNullable(key)
                .map(this::getKey)
                .map(template::hasKey)
                .orElse(false);
    }

    @Override
    public <A extends AuthorizationInfo> String save(A authorization, long timespan) {
        String key = UUID.randomUUID().toString().replaceAll("-","");
        AuthorizationRedisEntry entry = new AuthorizationRedisEntry();
        entry.setClasses(authorization.getClass().getName());
        entry.setJson(gson.toJson(authorization));
        this.template.opsForValue()
                .set(key,gson.toJson(entry), configuration.getAuthorization().getTimeout(), TimeUnit.MILLISECONDS);
        return key;
    }

    @Override
    public void remove(String key) {
        this.template.delete(getKey(key));
    }

    @Override
    public Optional<AuthorizationInfo> get(String key) {
        return Optional.ofNullable(this.template.opsForValue().get(getKey(key)))
                .map(json -> gson.fromJson(json,AuthorizationRedisEntry.class))
                .map(entry -> {
                    try {
                        Class<? extends AuthorizationInfo> classes = (Class<? extends AuthorizationInfo>) Class.forName(entry.getClasses());
                        return gson.fromJson(entry.getJson(),classes);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    @Override
    public String renew(String key) {
        this.template.expire(getKey(key),configuration.getAuthorization().getTimeout(), TimeUnit.MILLISECONDS);
        return key;
    }
}
