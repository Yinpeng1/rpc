package com.xxx.rpc.client.annotationProxy;

import com.xxx.rpc.client.RpcProxy;
import com.xxx.rpc.client.annotation.RpcAutowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.util.Set;


public class AnnotatonSetProxy implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatonSetProxy.class);

    private String packageName;

    public AnnotatonSetProxy(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ScanAnnotation scanAnnotation = new ScanAnnotation();
        try{
            Set<Class<?>> clsList = scanAnnotation.getClasses(packageName);
            if (clsList != null && clsList.size() > 0) {
                for (Class<?> cls : clsList) {
                    Field[] fields = cls.getDeclaredFields();
                    for (Field field: fields) {
                        if (field.getAnnotation(RpcAutowired.class) != null){
                            field.setAccessible(true);
                            field.set(applicationContext.getBean(cls), applicationContext.getBean(RpcProxy.class).create(field.getType()));
                        }
                    }
                }
            }
        } catch (Exception e){
            LOGGER.error("scan class error");
        }
    }
}
