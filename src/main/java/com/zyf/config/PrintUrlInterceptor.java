package com.zyf.config;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Endpoint;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;
import org.noear.solon.core.route.Routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 获取所有系统url，拦截所有非系统url，打印所有请求url
 *
 * @author zyf
 * @date 2024/05/16
 */
@Slf4j
@Component(index = 800)
public class PrintUrlInterceptor implements RouterInterceptor {

    public static final Collection<Routing<Handler>> routeList = new ArrayList<>();

    private static final List<String> igloneList = new ArrayList<>();
    private static final List<String> urls = new ArrayList<>();
    private static final boolean enabled = true;

    static {
        igloneList.add("/connection");
        igloneList.add("/realIp");
        igloneList.add("/ip");
    }

    private static final Cache<String, Integer> notUrlCache = CacheUtil.newFIFOCache(100);

    private static String getPath(Context ctx) {
        String path = ctx.uri().getPath();
        String contextPath = Solon.cfg().get("server.contextPath");
        if (StrUtil.isNotBlank(contextPath)) {
            contextPath = StrUtil.addPrefixIfNot(contextPath, "/");
            contextPath = StrUtil.removeSuffix(contextPath, "/");
            contextPath = StrUtil.removePrefix(contextPath, "/!");
            path = StrUtil.removePrefix(path, contextPath);
        }
        path = StrUtil.subBefore(path, '?', false);
        path = StrUtil.subBefore(path, '&', false);
        return path;
    }

    @Init
    public void init() {
        Solon.context().lifecycle(() -> {
            routeList.addAll(Solon.app().router().getAll(Endpoint.main));
        });
        for (Routing<Handler> handlerRouting : routeList) {
            final MethodType method = handlerRouting.method();
            final String path = handlerRouting.path();
            log.info("初始化加载urls: {} {}", method.name, path);
        }
    }

    /**
     * 拦截处理（包围式拦截） //和过滤器的 doFilter 类似，且只对路由器范围内的处理有效
     */
    @Override
    public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("PrintUrlInterceptor");
        }
        if (!enabled) {
            chain.doIntercept(ctx, mainHandler);
            return;
        }
        final String realIp = ctx.realIp();
        String sourcePath = ctx.uri().getPath();
        String path = getPath(ctx);
        if (log.isDebugEnabled()) {
            log.debug("{} 请求url: {}", realIp, sourcePath);
        }
        if (igloneList.contains(path)) {
//            log.info("{} 请求url: {}", realIp, sourcePath);
            chain.doIntercept(ctx, mainHandler);
            return;
        }
        if (notUrlCache.containsKey(sourcePath)) {
            log.info("{} 未找到匹配的url: {}", realIp, sourcePath);
        } else if (urls.contains(sourcePath)) {
            log.info("{} 请求url: {}", realIp, sourcePath);
            chain.doIntercept(ctx, mainHandler);
        } else {
            boolean isFind = false;
            for (Routing<Handler> routing : routeList) {
                if (routing.matches(routing.method(), path)) {
                    urls.add(sourcePath);
                    chain.doIntercept(ctx, mainHandler);
                    isFind = true;
                    break;
                }
            }
            if (isFind) {
                log.info("{} 请求url: {}", realIp, sourcePath);
            } else {
                log.info("{} 未找到匹配的url: {}", realIp, sourcePath);
                notUrlCache.put(sourcePath, 1);
            }
        }
    }

    /**
     * 提交结果（ render 执行前调用）//不要做太复杂的事情
     */
    @Override
    public Object postResult(Context ctx, Object result) throws Throwable {
        return result;
    }
}
