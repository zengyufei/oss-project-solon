package com.zyf.controller.apis;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Result;

/**
 * 直接抛异常测试
 */
@Slf4j
@Mapping("api")
@Controller
public class ErrorController {

    @SaCheckLogin
    @Mapping("/error")
    public Result<Integer> error() throws Exception {
        return Result.succeed(1 / 0);
    }

//    @Post
//    @Mapping("/json")
//    public Result<String> testJson(@CurrentUser LoginInfo loginInfo) throws Exception {
//        return Result.succeed(JSONUtil.toJsonPrettyStr(loginInfo));
//    }
}
