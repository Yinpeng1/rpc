package com.yp.rpc.register.redis;

import com.alibaba.fastjson.JSON;
import com.xxx.rpc.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RedisServiceDiscover implements ServiceDiscovery{

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisServiceDiscover.class);

    private Map<String, JedisPool> jedisPools = new ConcurrentHashMap<>();

    public RedisServiceDiscover(String redisAddress) {
        if (redisAddress != null){
            for (int i = 0; i < redisAddress.split(",").length; i++) {
                //map的key为redis服务器地址和端口
                jedisPools.put(redisAddress.split(",")[i], new JedisPool(redisAddress.split(",")[i].split(":")[0], Integer.valueOf(redisAddress.split(",")[i].split(":")[1])));
            }
        }
    }

    @Override
    public String discover(String serviceName) {
        try{
            for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                JedisPool jedisPool = entry.getValue();
                Jedis jedis = jedisPool.getResource();
                try{
                    if (jedis.smembers(serviceName) != null){
                        return jedis.srandmember(serviceName);
                    }
                } catch (Throwable t){
                    LOGGER.error("Failed to discover service from redis registry. registry: " + jedis + ", service: " + serviceName + ", cause: " + t.getMessage(), t);
                }finally {
                    jedis.close();
                }
            }
        } catch (Throwable t){
            throw new RuntimeException("jedis pool is unuseable");
        }
        return null;
    }
}
