package com.zyf.config;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component(index = 900)
public class CheckLimitFilter implements Filter {

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("CheckLimitFilter");
        }
        CheckRateLimitUtil.checkLimit(4, "请勿刷接口!");
        chain.doFilter(ctx);
    }


    /**
     * 滑动时间窗口限流工具
     * 本限流工具只适用于单机版，如果想要做全局限流，可以按本程序的思想，用redis的List结构去实现
     *
     * @author zyf
     * @date 2024/05/16
     */
    @Slf4j
    public static class CheckRateLimitUtil {

        /**
         * 队列id和队列的映射关系，队列里面存储的是每一次通过时候的时间戳，这样可以使得程序里有多个限流队列
         */
        private static final Cache<String, List<Long>> cache;

        static {
            cache = CacheUtil.newFIFOCache(1000, 10 * 1000);
            cache.setListener((key, cachedObject) -> {
                if (log.isTraceEnabled()) {
                    log.trace(key + " 过期被删除!");
                }
            });
        }


        public static void checkLimit(int counted, String msg) {
            final Context ctx = Context.current();
            // 代码执行到此，说明目标方法上有 RateLimit 注解，所以需要校验这个请求是不是在刷接口
            // 获取请求IP地址

            String ip = ctx.realIp();
            // 请求url路径
            String uri = ctx.uri().getPath();
            // 存到redis中的key
            String key = "RateLimit:" + ip + ":" + uri;

            final boolean isGo = isGo(key, counted, 2000L);
            if (!isGo) {
                log.warn("{} 访问 {} 重复太多次,出现限制访问!", ip, uri);
                throw new IllegalStateException(msg);
            }
        }


        /**
         * 滑动时间窗口限流算法
         * 在指定时间窗口，指定限制次数内，是否允许通过
         *
         * @param listId     队列id
         * @param count      限制次数
         * @param timeWindow 时间窗口大小
         * @return 是否允许通过
         */
        public static synchronized boolean isGo(String listId, int count, long timeWindow) {
            // 获取当前时间
            long nowTime = System.currentTimeMillis();
            // 根据队列id，取出对应的限流队列，若没有则创建

            List<Long> list;
            if (cache.containsKey(listId)) {
                list = cache.get(listId);
            } else {
                list = new LinkedList<>();
                cache.put(listId, list);
            }
            // 如果队列还没满，则允许通过，并添加当前时间戳到队列开始位置
            if (list.size() < count) {
                list.add(0, nowTime);
                cache.put(listId, list);
                return true;
            }

            // 队列已满（达到限制次数），则获取队列中最早添加的时间戳
            Long farTime = list.get(count - 1);
            // 用当前时间戳 减去 最早添加的时间戳
            if (nowTime - farTime <= timeWindow) {
                // 若结果小于等于timeWindow，则说明在timeWindow内，通过的次数大于count
                // 不允许通过
                return false;
            } else {
                // 若结果大于timeWindow，则说明在timeWindow内，通过的次数小于等于count
                // 允许通过，并删除最早添加的时间戳，将当前时间添加到队列开始位置
                list.remove(count - 1);
                list.add(0, nowTime);
                cache.put(listId, list);
                return true;
            }
        }
    }

}
