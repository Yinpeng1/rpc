package com.xxx.rpc.sample.client;

import com.xxx.rpc.client.RpcProxy;
import com.xxx.rpc.registry.ServiceDiscovery;
import com.xxx.rpc.sample.api.HelloService;
import com.yp.rpc.register.redis.RedisServiceDiscover;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class HelloClient3 {

    public static void main(String[] args) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);
        HelloService helloService = rpcProxy.create(HelloService.class);

        int loopCount = 500;


        long start = System.currentTimeMillis();

        for (int i = 0; i < loopCount; i++) {
           executorService.submit(() -> {
               String result = helloService.hello("World");
               System.out.println(result);
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(100000, TimeUnit.SECONDS);

        long time = System.currentTimeMillis() - start;
        System.out.println("loop: " + loopCount);
        System.out.println("time: " + time + "ms");
        System.out.println("tps: " + (double) loopCount / ((double) time / 1000));

        System.exit(0);
    }
}
