package gcy.system.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class JvmLockManager {

    private static final ConcurrentHashMap<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();

    private JvmLockManager() {
    }

    public static ReentrantLock getLock(String lockKey) {
        return LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock());
    }
}
