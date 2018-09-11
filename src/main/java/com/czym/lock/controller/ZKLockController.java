package com.czym.lock.controller;

import com.czym.lock.utils.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * czym
 */
@RestController
@RequestMapping("/zk")
@Slf4j
public class ZKLockController {

    //初始化商品库存
    public static int stockTempCounts = 9;

    @Autowired
    private DistributedLock distributedLock;

    //模拟第一次请求（同时）
    @GetMapping("/lock1")
    public String zkLock1() {
        return displayReduceCounts();
    }

    //模拟第二次请求（同时）
    @GetMapping("/lock2")
    public String zkLock2() {
        return displayReduceCounts();
    }

    //模拟第三次请求（同时）
    @GetMapping("/lock3")
    public String zkLock3() {
        return displayReduceCounts();
    }

    public String displayReduceCounts(){
        try{
            int buyCounts = 6;

            distributedLock.getLock();
            // 1. 判断库存
            int stockCounts = stockTempCounts;
            if (stockCounts < buyCounts) {

                log.info("库存剩余{}件，用户需求量{}件，库存不足，订单创建失败...",
                        stockCounts, buyCounts);
                distributedLock.releaseLock();
                return "库存不足";
            }

            Thread.sleep(5000);

            // 2. 创建订单
            boolean isOrderCreated = true;

            // 3. 创建订单成功后，扣除库存
            if (isOrderCreated) {
                log.info("订单创建成功...");
                stockTempCounts = stockTempCounts - buyCounts;
            } else {
                distributedLock.releaseLock();
                log.info("订单创建失败...");
                return "订单创建失败";
            }
            distributedLock.releaseLock();
        }catch (Exception e){
            distributedLock.releaseLock();
            log.error(e.getMessage(), e);
        }
        return "订单创建成功";
    }

}
