package com.zyf.config;

import com.zyf.utils.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;

@Slf4j
@Component(index = Integer.MAX_VALUE - 1)
public class MsgInterceptor implements RouterInterceptor {

    /**
     * 拦截处理（包围式拦截） //和过滤器的 doFilter 类似，且只对路由器范围内的处理有效
     */
    @Override
    public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("MsgInterceptor");
        }
        chain.doIntercept(ctx, mainHandler);
    }

    /**
     * 提交结果（ render 执行前调用）//不要做太复杂的事情
     */
    @Override
    public Object postResult(Context ctx, Object result) throws Throwable {
        MsgUtil.clear();
        return result;
    }
}
