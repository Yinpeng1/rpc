package com.xxx.rpc.registry.zookeeper;

import com.xxx.rpc.registry.ServiceRegistry;
import com.xxx.rpc.registry.zookeeper.monitor.ZKMonitor;
import org.I0Itec.zkclient.*;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于 ZooKeeper 的服务注册接口实现
 *
 * @author yinpeng
 * @since 1.0.0
 */
public class ZooKeeperServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private final ZkClient zkClient;

    private final ZKMonitor zkMonitor;

    public ZooKeeperServiceRegistry(String zkAddress) {
        // 创建 ZooKeeper 客户端
        zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        zkMonitor = new ZKMonitor(zkClient);
        zkMonitor.monitorZKStatus();
        LOGGER.debug("connect zookeeper");
    }

    @Override
    public void register(String serviceName, String serviceAddress){
        // 创建 registry 节点（持久）
        String registryPath = Constant.ZK_REGISTRY_PATH;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            LOGGER.debug("create registry node: {}", registryPath);
        }
        // 创建 service 节点（持久）
        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            LOGGER.debug("create service node: {}", servicePath);
        }
        // 创建 address 节点（临时）
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        LOGGER.debug("create address node: {}", addressNode);
    }

    @Override
    public void unregister(String serviceName) {
        try {
//           只能删除空目录节点
            zkClient.delete(Constant.ZK_REGISTRY_PATH + "/" +serviceName);
        } catch (Throwable e){
            throw new RuntimeException("Failed to unregister " + serviceName, e);
        }
    }

}