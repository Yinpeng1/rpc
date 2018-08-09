package com.xxx.rpc.registry.zookeeper.monitor;

import com.xxx.rpc.registry.zookeeper.Constant;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class ZKMonitor {

    private static final Logger logger = LoggerFactory.getLogger(ZKMonitor.class);

    private ZkClient zkClient;

    public ZKMonitor(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public void  monitorZKStatus(){
        zkClient.subscribeChildChanges(Constant.ZK_REGISTRY_PATH, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                logger.debug("child {} has changed", Constant.ZK_REGISTRY_PATH + parentPath);
            }
        });

        zkClient.subscribeDataChanges(Constant.ZK_REGISTRY_PATH, new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                logger.debug("data {} has changed", Constant.ZK_REGISTRY_PATH + dataPath );
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                logger.debug("data {} is deleted", Constant.ZK_REGISTRY_PATH + dataPath );
            }
        });

        zkClient.subscribeStateChanges(new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                logger.debug("state is changing");
            }

            @Override
            public void handleNewSession() throws Exception {
                logger.debug("new session has been in, state is changing");
            }

            @Override
            public void handleSessionEstablishmentError(Throwable error) throws Exception {
                logger.debug("session establish error");
            }
        });
    }
}
