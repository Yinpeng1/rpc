package com.yp.rpc.register.redis.heart;

import com.xxx.rpc.common.bean.RpcRequest;
import com.xxx.rpc.common.bean.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartCheckProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartCheckProxy.class);

    public HeartCheckProxy() {

    }

    public Object checkHeartBreak(RpcRequest rpcRequest, String host, int port){
        try{
            HeartBreakHandler client = new HeartBreakHandler(host, port);
            RpcResponse response = client.send(rpcRequest);
            if (response == null) {
                throw new RuntimeException("response is null");
            }
            // 返回 RPC 响应结果
            if (response.hasException()) {
                throw response.getException();
            } else {
                return response.getResult();
            }
        }catch (Throwable e){
            LOGGER.error("check heart fail");
        }
        return null;
    }
}
