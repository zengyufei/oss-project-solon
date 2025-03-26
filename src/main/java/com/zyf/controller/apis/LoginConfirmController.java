package com.zyf.controller.apis;

import com.zyf.controller.websocket.WebSocketDemo;
import com.zyf.utils.ScanUtil;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Valid;


/**
 * 登录控制器
 *
 * @author zyf
 * @date 2024/03/22
 */
@Valid
@Mapping("/api")
@Controller
public class LoginConfirmController {

    @Inject
    private WebSocketDemo webSocketDemo;

    @Get
    @Mapping("/login/confirm")
    public Result<String> loginConfirm() {
        final Context ctx = Context.current();
        final String id = ctx.param("id");
        ScanUtil.set(id, "3");
        webSocketDemo.sendMessage(id, "2");
        return Result.succeed("恭喜!登录成功!");
    }


}
