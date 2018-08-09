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

    private Map<String, JedisPool> jedisPool = new ConcurrentHashMap();

    private Jedis jedis;

    public RedisServiceRegister(Jedis jedis) {
        this.jedis = jedis;
        LOGGER.info("start the redis server register");
        expireExecutor.scheduleWithFixedDelay(() -> {
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
//                if (jedis.hkeys(Constant.REDIS_KEY) != null){
//                    for (String field : jedis.hkeys(Constant.REDIS_KEY)){
//                        RpcRequest request = new RpcRequest();
//                        request.setCheckStatus(true);
//                        HeartCheckProxy rpcProxy = new HeartCheckProxy();
//                        String result = rpcProxy.checkHeartBreak(request, jedis.hget(Constant.REDIS_KEY, field).split(":")[0], Integer.valueOf(jedis.hget(Constant.REDIS_KEY, field).split(":")[1])).toString();
//                        if (!result.equals(Constant.status)){
//                            jedis.hdel(Constant.REDIS_KEY, jedis.hget(Constant.REDIS_KEY, field).split(":")[0]);
//                            LOGGER.info("send heartbreak to serviceName {} address {} fail and it will be delete", jedis.hget(Constant.REDIS_KEY, field).split(":")[0]);
//                        }
//                    }
//                }
            }catch (Throwable e){
                LOGGER.error("schedule check service thread start error because {}", e);
            }
        },Constant.REDIS_INITIAL_TIME, Constant.REDIS_CHECK_HEART_PERIOD, TimeUnit.MILLISECONDS);
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            jedis.sadd(serviceName, serviceAddress);
            LOGGER.info("new service is registering serviceName {} serviceAddress {} ", serviceName, serviceAddress);
//            jedis.hset("register", key, value);
        }catch (Throwable t){
            LOGGER.debug("Failed to register service to redis registry. registry: " + jedis + ", service: " + serviceName + ", cause: " + t.getMessage(), t);
        }
    }

    @Override
    public void unregister(String serviceName) {

    }
}
