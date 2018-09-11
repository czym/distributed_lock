package com.czym.lock.controller;

import com.czym.lock.utils.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * czym
 */
@RestController
@RequestMapping("/redis")
@Slf4j
public class RedisLockController {

    //商品库存锁
    public static final String DISTRIBUTED_LOCK = "DISTRIBUTED_LOCK";

    //模拟第一次请求（同时）
    @GetMapping("/lock1")
    public String zkLock1() throws Exception {
        return displayReduceCounts();
    }

    //模拟第二次请求（同时）
    @GetMapping("/lock2")
    public String zkLock2() throws Exception {
        return displayReduceCounts();
    }

    //模拟第三次请求（同时）
    @GetMapping("/lock3")
    public String zkLock3() throws Exception {
        return displayReduceCounts();
    }

    public String displayReduceCounts() throws Exception{
        long lockTimeout = 5000;
        Long setnxResult = RedisShardedPoolUtil.setnx(DISTRIBUTED_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if(setnxResult != null && setnxResult.intValue() == 1){
            log.info("获取锁成功》》》》》》》》》》》》》");
            reduceCount(DISTRIBUTED_LOCK);
        }else{
            //未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(DISTRIBUTED_LOCK);
            if(lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)){
                String getSetResult = RedisShardedPoolUtil.getSet(DISTRIBUTED_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
                //再次用当前时间戳getset。
                //返回给定的key的旧值，->旧值判断，是否可以获取锁
                //当key没有旧值时，即key不存在时，返回nil ->获取锁
                //这里我们set了一个新的value值，获取旧的值。
                if(getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr,getSetResult))){
                    //真正获取到锁
                    reduceCount(DISTRIBUTED_LOCK);
                }else{
                    log.info("获取锁失败》》》》》》》》》》》》》");
                    return "获取锁失败";
                }
            }else{
                log.info("获取锁失败》》》》》》》》》》》》》");
                return "获取锁失败";
            }
        }
        return "获取锁成功";
    }

    private void reduceCount(String lockName) throws Exception{
        RedisShardedPoolUtil.expire(lockName,5);//有效期5秒，防止死锁

        /**
         * 业务逻辑
         */
        Thread.sleep(5000);


        RedisShardedPoolUtil.del(DISTRIBUTED_LOCK);
        log.info("===============================");
    }

}
