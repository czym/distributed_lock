package com.czym.lock.config;

import com.czym.lock.utils.DistributedLock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * czym
 */
@Configuration
public class ZKConfig {

    @Bean
    public RetryNTimes retryPolicy() {
        return new RetryNTimes(3, 1000);
    }

    @Bean
    public CuratorFramework client() {
        CuratorFramework client = CuratorFrameworkFactory
                .newClient("127.0.0.1:2181", 10000, 5000, retryPolicy());
        return client;
    }

    @Bean
    public DistributedLock distributedLock() {
        CuratorFramework client = client();
        client.start();
        DistributedLock distributedLock = new DistributedLock("ZK-locks-nameSpace",client());
        distributedLock.init();
        return distributedLock;
    }

}
