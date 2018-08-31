package com.xxx.rpc.registry.zookeeper.zkpool;

import com.xxx.rpc.registry.zookeeper.Constant;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ZKClientPool extends Pool<ZkClient> {

    private LinkedBlockingQueue<ZkClient> pool;

    private static GenericObjectPoolConfig config;

    static {
        config = new GenericObjectPoolConfig();
        config.setMinIdle(1);
        config.setMaxTotal(10);
        config.setMaxIdle(10);
        config.setTestOnBorrow(false);
    }
    public ZKClientPool() {
        this("127.0.0.1:2181");
    }

    public ZKClientPool(String zkAddress){
        super(config, new ZKFactory(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT));
    }

    @Override
    public ZkClient getResource() {
        ZkClient zkClient = super.getResource();
        return zkClient;
    }

    @Override
    public void returnResource(ZkClient resource) {
        super.returnResource(resource);
    }

    public ZKClientPool (String zkAddress, int poolSize){
        this.pool = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            try {
                this.pool.put(new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT));
            } catch (InterruptedException e) {
                throw new RuntimeException("zk连接队列添加错误");
            }
        }
    }

    public void returnObj(ZkClient zkClient){
        try {
            this.pool.put(zkClient);
        } catch (InterruptedException e) {
            throw new RuntimeException("zk连接队列添加错误");
        }
    }

    public ZkClient getZkClient(){
        ZkClient zkClient;
        try {
            zkClient = this.pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("从zk连接队列获取连接错误");
        }
        return zkClient;
    }
}
