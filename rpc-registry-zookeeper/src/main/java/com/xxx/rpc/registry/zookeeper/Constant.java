package com.xxx.rpc.registry.zookeeper;

/**
 * 常量
 *
 * @author yinpeng
 * @since 1.0.0
 */
public interface Constant {

    int ZK_SESSION_TIMEOUT = 5000;
    int ZK_CONNECTION_TIMEOUT = 3000;

    String ZK_REGISTRY_PATH = "/registry";

    String status = "yes";
}