package com.zyf.controller.apis;

import cn.hutool.core.util.StrUtil;
import com.zyf.controller.websocket.WebSocketDemo;
import com.zyf.utils.ScanUtil;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Valid;

import java.net.URI;


/**
 * 登录控制器
 *
 * @author zyf
 * @date 2024/03/22
 */
@Valid
@Mapping("/api")
@Controller
public class ScanQrController {

    @Inject
    private WebSocketDemo webSocketDemo;

    @Get
    @Mapping("/scan")
    public Result<Boolean> scan(final Context ctx) {
        final String id = ctx.param("id");
        final String oldId = ScanUtil.get(id);
        if (StrUtil.isNotBlank(oldId)) {
            ScanUtil.set(id, "1");
            webSocketDemo.sendMessage(id, "1");

            final URI uri = ctx.uri();
            final String scheme = uri.getScheme();
            final String host = uri.getHost();
            final int port = uri.getPort();

            ctx.redirect(StrUtil.format("{}://{}:{}/view/qr/scan?id={}", scheme, host, port, id));

        }
        return Result.succeed(true);
    }


}
