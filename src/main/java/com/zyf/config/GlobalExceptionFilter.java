package com.zyf.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.noear.solon.core.handle.Result;

// 可以和其它异常处理合并一个过滤器
@Slf4j
@Component(index = 800)
public class GlobalExceptionFilter implements Filter {
    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("GlobalExceptionFilter");
        }
        try {
            chain.doFilter(ctx);
        } catch (Throwable e) {
            log.error("异常: ", e);
            if (StrUtil.equalsIgnoreCase(e.getMessage(), "writeBuffer has closed")) {
                ctx.setHandled(true);
                ctx.setRendered(true);
            } else {
                ctx.render(Result.failure(500, e.getMessage()));
            }
        }
    }
}
