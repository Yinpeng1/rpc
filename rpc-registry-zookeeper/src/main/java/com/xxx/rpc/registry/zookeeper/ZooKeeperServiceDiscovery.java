package com.xxx.rpc.registry.zookeeper;

import com.xxx.rpc.common.util.CollectionUtil;
import com.xxx.rpc.registry.ServiceDiscovery;
import com.xxx.rpc.registry.zookeeper.zkpool.ZKClientPool;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于 ZooKeeper 的服务发现接口实现
 *
 * @author yinpeng
 * @since 1.0.0
 */
public class ZooKeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    private static int MAX_SIZE = 10;

    private String zkAddress;

    private ZkClient zkClient;

    private ZKClientPool zkClientPool;

    public ZooKeeperServiceDiscovery(String zkAddress) {
        this.zkAddress = zkAddress;
        zkClientPool = new ZKClientPool(zkAddress, MAX_SIZE);
    }

    @Override
    public String discover(String name) {
        LOGGER.debug("connect zookeeper");
        // 创建 ZooKeeper 客户端
//        this.zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        this.zkClient = zkClientPool.getZkClient();
        try {
            // 获取 service 节点
            String servicePath = Constant.ZK_REGISTRY_PATH + "/" + name;
            if (!zkClient.exists(servicePath)) {
                throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
            }
            List<String> addressList = zkClient.getChildren(servicePath);
            if (CollectionUtil.isEmpty(addressList)) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }
            // 获取 address 节点
            String address;
            int size = addressList.size();
            if (size == 1) {
                // 若只有一个地址，则获取该地址
                address = addressList.get(0);
                LOGGER.debug("get only address node: {}", address);
            } else {
                // 若存在多个地址，则随机获取一个地址
                address = addressList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("get random address node: {}", address);
            }
            // 获取 address 节点的值
            String addressPath = servicePath + "/" + address;
            return zkClient.readData(addressPath);
        } finally {
            zkClientPool.returnObj(zkClient);
//            zkClient.close();
        }
    }
}