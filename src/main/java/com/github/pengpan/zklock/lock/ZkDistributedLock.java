package com.github.pengpan.zklock.lock;

import javafx.util.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 基于zookeeper实现的分布式锁
 * <p>
 * 说明：获取锁和释放锁必须在一个线程里面
 */
public class ZkDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(ZkDistributedLock.class);

    private final ThreadLocal<Pair<InterProcessMutex, String>> threadLocal = new ThreadLocal<>();

    private static CuratorFramework curatorFramework;

    public ZkDistributedLock() {
    }

    public ZkDistributedLock(CuratorFramework curatorFramework) {
        ZkDistributedLock.curatorFramework = curatorFramework;
    }

    /**
     * 获取分布式锁  默认自旋 直到锁可用
     */
    public boolean acquire(String lockKey) {
        try {
            InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/" + lockKey);
            lock.acquire();
            threadLocal.set(new Pair<>(lock, lockKey));
            return true;
        } catch (Exception e) {
            log.error("获取zk分布式锁异常,lockKey:{},e:", lockKey, e);
            return false;
        }
    }

    /**
     * 获取分布式锁
     */
    public boolean acquire(String lockKey, long time, TimeUnit unit) {
        try {
            InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/" + lockKey);
            if (lock.acquire(time, unit)) {
                threadLocal.set(new Pair<>(lock, lockKey));
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("获取zk分布式锁异常,lockKey:{},e:", lockKey, e);
            return false;
        }
    }

    /**
     * 释放锁
     */
    public void release() {
        String lockKey = null;
        try {
            // 当前线程中获取到pair   如果没有获取到锁 没有必要做释放
            Pair<InterProcessMutex, String> pair = threadLocal.get();
            if (pair == null) {
                return;
            }
            InterProcessMutex lock = pair.getKey();
            lockKey = pair.getValue();
            if (lock == null) {
                return;
            }
            if (!lock.isAcquiredInThisProcess()) {
                return;
            }
            lock.release();
        } catch (Exception e) {
            log.error("释放zk分布式锁异常,lockKey:{},e:", lockKey, e);
        } finally {
            threadLocal.remove();
        }
    }

}
