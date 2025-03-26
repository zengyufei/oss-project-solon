package com.zyf.config;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.slf4j.MDC;

@Slf4j
@Component(index = 1000)
public class TraceIdFilter implements Filter {
    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("TraceIdFilter");
        }
        String traceId = IdUtil.fastUUID();
        final String key = "X-TraceId";
        MDC.put(key, traceId);

        // 在 param/attr/header 均能获取到一个 "reqId" 的 uuid 值;
        ctx.paramSet(key, traceId);
        ctx.attrSet(key, traceId);
        ctx.headerAdd(key, traceId);
        chain.doFilter(ctx);
    }
}
