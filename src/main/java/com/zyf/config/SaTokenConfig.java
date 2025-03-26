package com.zyf.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.solon.integration.SaTokenInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@Condition(onProperty = "${solon.env} = prod")
public class SaTokenConfig {
    private static final List<String> igloneList = new ArrayList<>();

    static {
        igloneList.add("/");
        igloneList.add("/connection");
        igloneList.add("/realIp");
        igloneList.add("/ip");
        igloneList.add("/view/login");
        igloneList.add("/api/scan");
        igloneList.add("/api/login/oauth");
        igloneList.add("/api/login/confirm");
        igloneList.add("/api/login");
        igloneList.add("/file/download");
        igloneList.add("/api/shortUrl/**");
        igloneList.add("/api/file/downloadFile/**");
        igloneList.add("/api/v2/file/downloadFile/**");
        igloneList.add("/api/error");
        igloneList.add("/view/qr/login");
        igloneList.add("/view/qr/scan");
        igloneList.add("/api/json");
    }

    @Bean(index = Integer.MAX_VALUE - 2)  //-100，是顺序位（低值优先）
    public SaTokenInterceptor saTokenInterceptor() {
        return new SaTokenInterceptor()
                // 指定 [拦截路由] 与 [放行路由]
                .addInclude("/**").addExclude("/favicon.ico")

                // 认证函数: 每次请求执行
                .setAuth(req -> {
                    SaRouter
                            .notMatch(igloneList)
                            .match("/**", () -> {
                                if (log.isDebugEnabled()) {
                                    final Context current = Context.current();
                                    log.debug("检查 [{}] 是否登录: {}", current.realIp(), current.uri().getPath());
                                }
                                StpUtil.checkLogin();
                            });
                })

                // 异常处理函数：每次认证函数发生异常时执行此函数 //包括注解异常
                .setError(e -> {
                    final Context current = Context.current();
                    log.error(StrUtil.format("{} 请求 {} 发生 {}", current.realIp(), current.uri().getPath(), e.getMessage()));
                    return Result.succeed(e.getMessage());
                })

                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(req -> {
//        log.info("SaTokenInterceptor");
                    // ---------- 设置一些安全响应头 ----------
                    SaHolder.getResponse()
                            // 服务器名称
                            .setServer("oss")
                            // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
                            .setHeader("X-Frame-Options", "SAMEORIGIN")
                            // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            // 禁用浏览器内容嗅探
                            .setHeader("X-Content-Type-Options", "nosniff");
                });
    }
}
