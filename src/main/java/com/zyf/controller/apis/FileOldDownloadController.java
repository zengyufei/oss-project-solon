package com.zyf.controller.apis;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.zyf.controller.ApiCodes;
import com.zyf.utils.AliyunUtil;
import com.zyf.utils.CheckUtil;
import com.zyf.utils.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.cloud.model.Media;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Valid;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载控制器
 *
 * @author zyf
 * @date 2024/03/22
 */
@Slf4j
@Valid
@Mapping("/api")
@Controller
public class FileOldDownloadController {

    private static void setAttachmentName(Context ctx, String fileName) {
        String newFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String outFileName = StrUtil.format("attachment;filename={};filename*=utf-8''{};charset=utf-8", newFileName, newFileName);
        ctx.headerSet("Content-Disposition", outFileName);
    }

    /**
     * 文件下载, 只接受 urlencode 的中文名或普通字符名
     *
     * @param ctx       ctx
     * @param fileName  文件名
     * @param parentDir 父级dir
     * @return {@link DownloadedFile}
     * @throws Throwable 可抛出
     */
    @Get
    @SaIgnore
    @Mapping("/file/downloadFile/{fileName}")
    public void download(Context ctx, @NotNull @NotBlank String fileName, String size, String parentDir) throws Throwable {
        getDownloadedFile(ctx, fileName, size, parentDir);
    }

    /**
     * 下载帖子
     *
     * @param ctx      ctx
     * @param fileName 文件名
     * @return {@link DownloadedFile}
     * @throws Throwable 可抛出
     */
    @Post
    @Get
    @SaIgnore
    @NotNull({"fileName"})  // 非NULL验证
    @NotBlank({"fileName"})  // 非Blank验证
    @Mapping("/file/download")
    public void downloadPost(Context ctx, String fileName, String size) throws Throwable {
        getDownloadedFile(ctx, fileName, size, null);
    }

    private void getDownloadedFile(Context ctx, String fileName, String size, String parentDir) throws Throwable {
        final String ip = ctx.realIp();
        String 身份 = "游客";

        final boolean isLogin = StpUtil.isLogin();
        boolean isPrivate = CheckUtil.keyExistsAndPass();
        if (!isLogin && !isPrivate && !CheckUtil.isOpenFile(fileName, null)) {
            final boolean checkAuthOrRsa = CheckUtil.checkAuthOrRsa(ctx);
            if (!checkAuthOrRsa) {
                ctx.setHandled(true);
                ctx.setRendered(true);
                log.debug("{} [{}]进行访问, 但是没有权限, 已拒绝!", ip, 身份);
                ctx.render(Result.failure(ApiCodes.CODE_4001016.getCode(), ApiCodes.CODE_4001016.getDescription()));
                return;
            }
        }
        if (isLogin) {
            身份 = "登录用户";
        } else if (isPrivate) {
            身份 = "秘钥";
        }
        if (StrUtil.isBlank(fileName)) {
            ctx.setHandled(true);
            // 如果没有令牌；直接设定结果
            log.debug("{} [{}]进行访问, 但是没有文件名, 已拒绝!", ip, 身份);
            ctx.render(Result.failure(ApiCodes.CODE_4001015.getCode(), ApiCodes.CODE_4001015.getDescription()));
            return;
        }

        log.debug("{} [{}]想进行下载, 下载文件[{}]", ip, 身份, fileName);

        String pathFileName = PathUtil.getPathFileName(parentDir, fileName);
        log.debug("{} [{}]去阿里云获取文件[{}]", ip, 身份, fileName);

        final Media media = AliyunUtil.downloadFile(pathFileName);
        // 使用 InputStream 实例化
        String contentType = "application/octet-stream";
        if (fileName.endsWith(".doc")) {
            contentType = "application/msword";
        } else if (fileName.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".xls")) {
            contentType = "application/octet-stream;charset=ISO8859-1";
        }

        final byte[] bytes = media.bodyAsBytes(true);
        log.debug("{} [{}][{}]服务端下载完毕, 下一步传输给用户!", ip, 身份, fileName);

        final int length = bytes.length;
        ctx.headerSet("Content-Length", length + "");
        ctx.headerSet("File-Size", length + "");
        ctx.headerSet("Content-Type", contentType);
        if (!StrUtil.equalsIgnoreCase(contentType, "image/png")) {
            setAttachmentName(ctx, fileName);
        }

        IoUtil.write(ctx.outputStream(), true, bytes);
    }

}
