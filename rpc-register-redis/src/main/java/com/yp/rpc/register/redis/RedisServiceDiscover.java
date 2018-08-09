package com.yp.rpc.register.redis;

import com.xxx.rpc.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;


public class RedisServiceDiscover implements ServiceDiscovery{

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisServiceDiscover.class);

    private Jedis jedis;
    public RedisServiceDiscover(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public String discover(String serviceName) {
        try{
            if (this.jedis.get(serviceName) != null){
                return jedis.get(serviceName);
            }
        } catch (Throwable t){
            LOGGER.error("Failed to subscribe service from redis registry. registry: " + jedis + ", service: " + serviceName + ", cause: " + t.getMessage(), t);

        }
//        for (Map.Entry<String, JedisPool> entry : RedisServiceRegister.getJedisPools().entrySet()) {
//            JedisPool jedisPool = entry.getValue();
//            try {
//                Jedis jedis = jedisPool.getResource();
//                try {
//                    if (jedis.get(serviceName) != null){
//                        return jedis.get(serviceName);
//                    } else {
//                        continue;
//                    }
//                } finally {
//                    jedis.close();
//                }
//            } catch (Throwable t) { // Try the next server
//                LOGGER.error("Failed to subscribe service from redis registry. registry: " + entry.getKey() + ", service: " + serviceName + ", cause: " + t.getMessage(), t);
//            }
//        }
        return null;
    }
}
