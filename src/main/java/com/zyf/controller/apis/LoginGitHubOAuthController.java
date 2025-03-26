package com.zyf.controller.apis;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zyf.controller.ApiCodes;
import com.zyf.controller.entity.GitHubOAuthResp;
import com.zyf.event.LogEvent;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.cloud.utils.http.HttpUtils;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Valid;

import java.util.HashMap;
import java.util.Map;


/**
 * 登录控制器
 *
 * @author zyf
 * @date 2024/03/22
 */
@Valid
@Mapping("/api")
@Controller
public class LoginGitHubOAuthController {


    @Inject("${base.username}")
    private String username;

    @Inject("${base.client_id}")
    private String clientId;
    @Inject("${base.client_secret}")
    private String clientSecret;

    @Get
    @Mapping("/login/oauth")
    public void loginOauth() throws Throwable {
        final Context ctx = Context.current();
        // code 就是我们需要的参数，有了这个就可以去申请 access_token，然后再去获取用户信息。
        final String code = ctx.param("code");
        System.out.println(code);

        final Map<String, String> map = new HashMap<>();
        map.put("client_id", clientId);
        map.put("client_secret", clientSecret);
        map.put("code", code);
        // map.put("redirect_uri", URLEncodeUtil.encode("http://localhost:8081/view/public/file"));

        final String post = HttpUtils.http("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .bodyJson(JSONUtil.toJsonStr(map))
                .post();
        System.out.println(post);
        if (StrUtil.containsAnyIgnoreCase(post, "access_token")) {
            final JSONObject json = JSONUtil.parseObj(post);
            final String token = json.getStr("access_token");
            final String tokenType = json.getStr("token_type");
            final String scope = json.getStr("scope");

            final String get = HttpUtils.http("https://api.github.com/user")
                    .header("Authorization", StrUtil.format("{} {}", tokenType, token))
                    .header("Accept", "application/json")
                    .get();
            System.out.println(get);
            final GitHubOAuthResp gitHubOAuthResp = JSONUtil.toBean(get, GitHubOAuthResp.class);
            final long id = gitHubOAuthResp.getId();
            final String login = gitHubOAuthResp.getLogin();
            if (13251679 == id) {
                EventBus.publish(new LogEvent(ctx.realIp() + " 进行 github 登录登录成功!"));

                // 第1步，先登录上
                StpUtil.login(username);

                ctx.redirect("/view/private/file/list");

                // 第2步，获取 Token  相关参数
                // SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
                // 第3步，返回给前端
                // return Result.succeed(tokenInfo);
            }
        }
        ctx.render(Result.failure(ApiCodes.CODE_315.getCode(), ApiCodes.CODE_315.getDescription()));
    }


}
