package com.xxx.rpc.registry.zookeeper.zkpool;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ZKFactory extends BasePooledObjectFactory<ZkClient> {

    private final String host;
    private final int connectionTimeout;
    private final int sessionTimeOut;

    public ZKFactory(String zkAddress, int sessionTimeOut, int connectionTimeout) {
        this.host = zkAddress;
        this.connectionTimeout = connectionTimeout;
        this.sessionTimeOut = sessionTimeOut;
    }
    @Override
    public ZkClient create() throws Exception {
        return new ZkClient(host, sessionTimeOut, connectionTimeout);
    }

    @Override
    public PooledObject<ZkClient> wrap(ZkClient obj) {
        return new DefaultPooledObject<>(obj);
    }
}
