package com.xxx.rpc.client.annotation;

import java.lang.annotation.*;

/**
 * RPC 服务注解（标注在服务实现类上）
 *
 * @author yinpeng
 * @since 1.0.0
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcAutowired {
}
