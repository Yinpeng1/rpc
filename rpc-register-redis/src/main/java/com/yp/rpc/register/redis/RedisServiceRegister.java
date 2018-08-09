package com.yp.rpc.register.redis;

import com.xxx.rpc.common.bean.RpcRequest;
import com.xxx.rpc.registry.ServiceRegistry;
import com.yp.rpc.register.redis.heart.HeartCheckProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.concurrent.*;

public class RedisServiceRegister implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisServiceRegister.class);

    private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(3);

    private static ThreadLocal<Map<String, JedisPool>> jedisPools = new ThreadLocal<>();

    private Map<String, JedisPool> jedisPool = new ConcurrentHashMap();

    private Jedis jedis;

//    public RedisServiceRegister(JedisPoolConfig jedisPoolConfig, String redisAddress, Jedis jedis) {
//        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
//        config.setTestOnBorrow(true);
//        config.setTestOnReturn(false);
//        config.setTestWhileIdle(false);
//        if (jedisPoolConfig.getMaxIdle() > 0)
//            config.setMaxIdle(jedisPoolConfig.getMaxIdle());
//        if (jedisPoolConfig.getMinIdle() > 0)
//            config.setMinIdle(jedisPoolConfig.getMinIdle());
//        if (jedisPoolConfig.getMaxTotal() > 0)
//            config.setMaxTotal(jedisPoolConfig.getMaxTotal());
//        if (jedisPoolConfig.getMaxWaitMillis() > 0)
//            config.setMaxWaitMillis(jedisPoolConfig.getMaxWaitMillis());
//        if (jedisPoolConfig.getNumTestsPerEvictionRun() > 0)
//            config.setNumTestsPerEvictionRun(jedisPoolConfig.getNumTestsPerEvictionRun());
//        if (jedisPoolConfig.getTimeBetweenEvictionRunsMillis() > 0)
//            config.setTimeBetweenEvictionRunsMillis(jedisPoolConfig.getTimeBetweenEvictionRunsMillis());
//        if (jedisPoolConfig.getMinEvictableIdleTimeMillis() > 0)
//            config.setMinEvictableIdleTimeMillis(jedisPoolConfig.getMinEvictableIdleTimeMillis());
//
//        String host = redisAddress.split(":")[0];
//        int port = Integer.valueOf(redisAddress.split(":")[1]);
//        this.jedisPool.put(redisAddress, new JedisPool(config, host, port));
//        jedis.set("register", jedisPool.toString());
//        jedisPools.set(jedisPool);
////        jedisPools.get().put(redisAddress, new JedisPool(config, host, port));
//        LOGGER.debug("start the redis server register");
//    }

    public RedisServiceRegister(Jedis jedis) {
        this.jedis = jedis;
        LOGGER.info("start the redis server register");
        expireExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (jedis.hkeys("register") != null){
                    for (String field : jedis.hkeys("register")){
                        RpcRequest request = new RpcRequest();
                        request.setCheckStatus(true);
                        HeartCheckProxy rpcProxy = new HeartCheckProxy();
                        String result = rpcProxy.checkHeartBreak(request, jedis.hget("register", field).split(":")[0], Integer.valueOf(jedis.hget("register", field).split(":")[1])).toString();
                        if (!result.equals("yes")){
                            jedis.hdel("register", jedis.hget("register", field).split(":")[0]);
                            LOGGER.info("send heartbreak to serviceName {} address {} fail and it will be delete", jedis.hget("register", field).split(":")[0]);
                        }
                    }
                }
            }catch (Throwable e){
                LOGGER.error("schedule check service thread start error because {}", e);
            }
        },3000, 3000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        String key = serviceName;
        String value = serviceAddress;
        try {
            jedis.hset("register", key, value);
        }catch (Throwable t){
            LOGGER.debug("Failed to register service to redis registry. registry: " + jedis + ", service: " + serviceName + ", cause: " + t.getMessage(), t);
        }
    }

    @Override
    public void unregister(String serviceName) {

    }
}
