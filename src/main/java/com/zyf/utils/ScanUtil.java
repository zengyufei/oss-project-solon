package com.zyf.utils;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;

public class ScanUtil {

    // 创建缓存，默认 30 * 1000毫秒过期
    static TimedCache<String, String> timedCache = CacheUtil.newTimedCache(30 * 1000);

    static {
        // 启动定时任务，每 10 * 1000毫秒清理一次过期条目，注释此行首次启动仍会清理过期条目
        timedCache.schedulePrune(10 * 1000);
    }

    public static void set(String key, String value) {
        timedCache.put(key, value);
    }

    public static String get(String key) {
        return timedCache.get(key);
    }
}
