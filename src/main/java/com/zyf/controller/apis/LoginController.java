package com.zyf.controller.apis;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.zyf.controller.entity.LoginInfo;
import com.zyf.event.LogEvent;
import org.noear.solon.annotation.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;


/**
 * 登录控制器
 *
 * @author zyf
 * @date 2024/03/22
 */
@Valid
@Mapping("/api")
@Controller
public class LoginController {

    @Inject("${base.username}")
    private String username;
    @Inject("${base.password}")
    private String password;

    /**
     * 登录
     *
     * @param loginInfo 登录信息
     * @return {@link Result}<{@link SaTokenInfo}>
     */
    @Post
    @Mapping("/login")
    public Result<SaTokenInfo> login(@Validated LoginInfo loginInfo) {
        final Context ctx = Context.current();
        if (!StrUtil.equalsIgnoreCase(loginInfo.getUsername(), username)) {
            EventBus.publish(new LogEvent(ctx.realIp() + " 进行登录, 用户名错误!"));
            return Result.failure("用户名/密码错误");
        }
        if (!StrUtil.equalsIgnoreCase(loginInfo.getPassword(), password)) {
            EventBus.publish(new LogEvent(ctx.realIp() + " 进行登录,密码错误!"));
            return Result.failure("用户名/密码错误");
        }
        EventBus.publish(new LogEvent(ctx.realIp() + " 进行登录登录成功!"));

        // 第1步，先登录上
        StpUtil.login(username);
        // 第2步，获取 Token  相关参数
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        // 第3步，返回给前端
        return Result.succeed(tokenInfo);
    }

    /**
     * 注销
     *
     * @return {@link Result}<{@link Boolean}>
     */
    @Get
    @Mapping("/logout")
    public Result<Boolean> logout() {
        StpUtil.logout();
        return Result.succeed(true);
    }

}
