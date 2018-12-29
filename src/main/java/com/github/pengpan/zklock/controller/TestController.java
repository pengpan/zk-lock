package com.github.pengpan.zklock.controller;

import com.github.pengpan.zklock.lock.ZkDistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @RequestMapping("lock")
    public String lock() {
        ZkDistributedLock lock = new ZkDistributedLock();
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        AtomicInteger integer = new AtomicInteger(0);
        for (int index = 0; index < 16; index++) {
            executorService.execute(() -> {
                try {
                    int i = integer.incrementAndGet();
                    boolean acquire = lock.acquire("1992", 3, TimeUnit.SECONDS);
                    if (acquire) {
                        log.info("-->获取到zk锁 --> || " + i);
                        Thread.sleep(500);
                    } else {
                        log.info("-->没有获取到zk锁 --> || " + i);
                    }
                } catch (Exception e) {
                    log.error("{}", e);
                } finally {
                    lock.release();
                }
            });
        }
        return null;
    }

    @RequestMapping("sec-kill/{redId}")
    public Map<String, Object> secKill(@PathVariable("redId") String redId) {
        ZkDistributedLock lock = new ZkDistributedLock();
        Map<String, Object> map = new HashMap<>();
        try {
            boolean acquire = lock.acquire(redId, 1, TimeUnit.SECONDS);
            if (!acquire) {
                //快速失败 1秒还没有抢到红包锁
                map.put("status", -1);
                map.put("model", "抢购的人数太多啦，挤爆了，再抢一次!");
                //因为http接口调用会有超时时间，这个地方你可以选择自旋 等待一直抢到锁。。但是客户端可能需要调整，看具体的交互
                return map;
            }
            map.put("status", 1);
            map.put("model", "卧槽  恭喜你已经抢到XXX,共有XX人抢到，还剩XXX");
            return map;
        } catch (Exception e) {
            log.error("{}", e);
        } finally {
            lock.release();
        }
        return null;
    }

}
