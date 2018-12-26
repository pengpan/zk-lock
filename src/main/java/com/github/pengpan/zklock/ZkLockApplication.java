package com.github.pengpan.zklock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ZkLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZkLockApplication.class, args);
    }

    @Value("${zookeeper.connectString}")
    private String connectString;

    @Value("${zookeeper.sessionTimeoutMs}")
    private int sessionTimeoutMs;

    @Value("${zookeeper.namespace}")
    private String namespace;

    @Bean
    public CuratorFramework initZookeeper() {
        CuratorFramework cf;
        //1 重试策略：重试时间为0s 重试10次  [默认重试策略:无需等待一直抢，抢10次］
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(0, 10);
        //2 通过工厂创建连接
        cf = CuratorFrameworkFactory
                .builder()
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(retryPolicy)
                .namespace(namespace)
                .build();
        //3 开启连接
        cf.start();
        return cf;
    }
}
