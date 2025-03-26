package com.zyf.utils;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class MsgUtil {

    private final static ThreadLocal<Map<String, Object>> THREAD_LOCAL_TENANT = new TransmittableThreadLocal<>();

    /**
     * 获取TTL中的数据
     *
     * @return
     */
    public static <T> T getMsg(String key) {
        return (T) Optional.ofNullable(THREAD_LOCAL_TENANT.get())
                .map(map->map.get(key))
                .orElse(null);
    }

    /**
     * 获取TTL中的数据
     *
     * @return
     */
    public static <T> T getMsg(String key, Object defaultObj) {
        return (T) Optional.ofNullable(THREAD_LOCAL_TENANT.get())
                .map(map->map.get(key))
                .orElse(defaultObj);
    }

    /**
     * TTL 设置数据<br/>
     *
     * @param msg
     */
    public static void setMsg(String key, Object obj) {
        Map<String, Object> map;
        if (THREAD_LOCAL_TENANT.get() == null) {
            map = new HashMap<>();
            THREAD_LOCAL_TENANT.set(map);
        } else {
            map = THREAD_LOCAL_TENANT.get();
        }
        map.put(key, obj);
    }

    /**
     * 清除当前线程中的数据
     * 慎用
     */
    public static void clear() {
        THREAD_LOCAL_TENANT.remove();
    }

}
