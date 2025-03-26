package com.zyf.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zyf.controller.ApiCodes;
import com.zyf.event.LogEvent;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;

import java.util.Date;
import java.util.List;

@Slf4j
public class CheckUtil {


    public static boolean isOpenFile(String mainName, String extName) {

        List<String> publicPrefixFiles = Solon.cfg().getList("files.public.prefix");
        List<String> publicSuffixFiles = Solon.cfg().getList("files.public.suffix");
        List<String> publicFullNameFiles = Solon.cfg().getList("files.public.fullName");

        boolean isSkip = false;
        if (CollUtil.isNotEmpty(publicPrefixFiles)) {
            for (String publicPrefixFile : publicPrefixFiles) {
                if (StrUtil.isBlank(publicPrefixFile)) {
                    continue;
                }
                if (StrUtil.startWithIgnoreCase(mainName, publicPrefixFile)) {
                    isSkip = true;
                    break;
                }
            }
        }
        if (!isSkip && CollUtil.isNotEmpty(publicSuffixFiles) && extName!=null) {
            for (String publicSuffixFile : publicSuffixFiles) {
                if (StrUtil.isBlank(publicSuffixFile)) {
                    continue;
                }
                if (StrUtil.endWithIgnoreCase(extName, publicSuffixFile)) {
                    isSkip = true;
                    break;
                }
            }
        }
        if (!isSkip && CollUtil.isNotEmpty(publicFullNameFiles)) {
            for (String publicFullNameFile : publicFullNameFiles) {
                if (StrUtil.isBlank(publicFullNameFile)) {
                    continue;
                }
                if (StrUtil.equalsIgnoreCase(mainName + "." + extName, publicFullNameFile)) {
                    isSkip = true;
                    break;
                }
            }
        }
        return isSkip;
    }


    public static boolean checkAuthOrRsa(Context ctx) throws Throwable {
        final boolean isLogin = StpUtil.isLogin();
        if (isLogin) {
            return true;
        }

        boolean isCheckToken = Solon.cfg().getBool("base.checkToken", true);
        if (isCheckToken) {
            String key = ctx.param("key");
            if (StrUtil.isBlank(key)) {
                // 设为已处理（主接口就不会进去了）
                ctx.setHandled(true);
                ctx.setRendered(true);
                // 如果没有令牌；直接设定结果
                EventBus.publish(new LogEvent(ctx.realIp() + " 进行访问, 但是没有登录, 已拒绝!"));
                ctx.render(Result.failure(ApiCodes.CODE_4001013.getCode(), ApiCodes.CODE_4001013.getDescription()));
                return false;
            } else {
                String username = Solon.cfg().get("base.username");
                final String decrypt = decodeData(key);
                final JSONObject json = JSONUtil.parseObj(decrypt);
                final String name = json.getStr("name");
                if (!StrUtil.equals(username, name)) {
                    // 设为已处理（主接口就不会进去了）
                    ctx.setHandled(true);
                    ctx.setRendered(true);
                    // 如果没有令牌；直接设定结果
                    EventBus.publish(new LogEvent(ctx.realIp() + " 通过 秘钥 进行访问, 但是错误, 已拒绝!!"));
                    ctx.render(Result.failure(ApiCodes.CODE_4001013.getCode(), ApiCodes.CODE_4001013.getDescription()));
                    return false;
                }
                final Date time = json.getDate("time");
                if (time == null || DateUtil.date().after(time)) {
                    // 设为已处理（主接口就不会进去了）
                    ctx.setHandled(true);
                    ctx.setRendered(true);
                    // 如果没有令牌；直接设定结果
                    EventBus.publish(new LogEvent(ctx.realIp() + " 通过 秘钥 进行访问, 但是日期无效, 已拒绝!!!"));
                    ctx.render(Result.failure(ApiCodes.CODE_4001013.getCode(), ApiCodes.CODE_4001013.getDescription()));
                    return false;
                }
                MsgUtil.setMsg("private", "private");
                EventBus.publish(new LogEvent(ctx.realIp() + " 成功通过 秘钥 进行访问!"));
                return true;
            }
        } else {
            return true;
        }
    }

    public static String decodeData(String key) throws Exception {
        final ClassPathResource classPathResource = new ClassPathResource("private.key");
        final String privateKey = classPathResource.readUtf8Str();
        return RsaUtil.decodePrivateKey(key, privateKey);
    }

    public static boolean keyExistsAndPass() throws Exception {
        Context ctx = Context.current();
        String key = ctx.param("key");
        if (StrUtil.isNotBlank(key)) {
            String username = Solon.cfg().get("base.username");
            final String decrypt = CheckUtil.decodeData(key);
            final JSONObject json = JSONUtil.parseObj(decrypt);
            final String name = json.getStr("name");
            final Date time = json.getDate("time");
            return StrUtil.equalsIgnoreCase(username, name) && !(time == null || DateUtil.date().after(time));
        }
        return false;
    }
}
