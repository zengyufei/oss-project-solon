package com.zyf.config.rateLimit;

import com.zyf.config.CheckLimitFilter;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;

import java.lang.annotation.*;

/**
 * 用于防刷限流的注解
 * 默认是5秒内只能调用一次
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流的key
     */
    String key() default "limit:";

    /**
     * 周期,单位是秒
     */
    int cycle() default 5;

    /**
     * 请求次数
     */
    int count() default 5;

    /**
     * 默认提示信息
     */
    String msg() default "请勿重复点击";

    @Slf4j
    class RateLimitInterceptor implements Interceptor, RouterInterceptor {

        Context ctx;

        @Override
        public Object doIntercept(Invocation inv) throws Throwable {
//            log.info("RateLimitInterceptor");
            if (ctx != null) {
                RateLimit rateLimit = inv.method().getAnnotation(RateLimit.class);
                if (rateLimit == null) {
                    // 说明目标方法上没有 RateLimit 注解
                    return inv.invoke();
                }
                final int counted = rateLimit.count();
                final String msg = rateLimit.msg();

                CheckLimitFilter.CheckRateLimitUtil.checkLimit(counted, msg);

                return inv.invoke();
            }
            return inv.invoke();
        }


        // 获取请求的归属IP地址
        @Override
        public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
            this.ctx = ctx;
            chain.doIntercept(ctx, mainHandler);
        }

    }

}
