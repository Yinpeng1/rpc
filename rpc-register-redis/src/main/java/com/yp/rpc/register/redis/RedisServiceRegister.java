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

    private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private Map<String, JedisPool> jedisPools = new ConcurrentHashMap<>();

    public RedisServiceRegister(String redisAddress) {
        if (redisAddress != null){
            for (int i = 0; i < redisAddress.split(",").length; i++) {
                //map的key为redis服务器地址和端口
                jedisPools.put(redisAddress.split(",")[i], new JedisPool(redisAddress.split(",")[i].split(":")[0], Integer.valueOf(redisAddress.split(",")[i].split(":")[1])));
            }
        }
        LOGGER.info("start the redis server register");
        expireExecutor.scheduleWithFixedDelay(() -> {
            try {
                for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                    JedisPool jedisPool = entry.getValue();
                    Jedis jedis = jedisPool.getResource();
                    try {
                        if (jedis.keys("*") != null){
                            /**拿到所有的key*/
                            for (String s : jedis.keys("*")){
                                if (jedis.type(s).equals("set")){
                                    for (String t :jedis.smembers(s)){
                                        RpcRequest request = new RpcRequest();
                                        request.setCheckStatus(true);
                                        HeartCheckProxy rpcProxy = new HeartCheckProxy();
                                        Object result = rpcProxy.checkHeartBreak(request, t.split(":")[0], Integer.valueOf(t.split(":")[1]));
                                        if (result == null || !(result.toString()).equals(Constant.status)){
                                            jedis.srem(s, t);
                                            LOGGER.info("send heartbreak to serviceName {} address {} fail and it will be delete", s, t);
                                        }
                                    }
                                }
                            }
                        }
                    }finally {
                     jedis.close();
                    }
                }
            }catch (Throwable e){
                LOGGER.error("schedule check service thread start error because {}", e);
            }
        },Constant.REDIS_INITIAL_TIME, Constant.REDIS_CHECK_HEART_PERIOD, TimeUnit.MILLISECONDS);
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                JedisPool jedisPool = entry.getValue();
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.sadd(serviceName, serviceAddress);
                    LOGGER.info("new service is registering serviceName {} serviceAddress {} ", serviceName, serviceAddress);
                } finally {
                    jedis.close();
                }
            }
        }catch (Throwable t){
            LOGGER.debug("Failed to register service to redis registry service: " + serviceName + ", cause: " + t.getMessage(), t);
        }
    }

    @Override
    public void unregister(String serviceName) {

    }
}
