package com.zyf.controller.apis;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.ArrayUtil;
import com.zyf.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Result;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.Valid;


/**
 * 接口开发，与控制器开发差不多的; 但进入网关的接口，要用 @Component 注解
 */
// 这个注解一定要加类上（或者基类上）
@Slf4j
@Valid
@Mapping("/api")
@Controller
public class FileUploadController {

    @Inject
    private FileUploadService fileUploadService;

    // 文件上传
    @Post
    @SaIgnore
    @Mapping("/file/upload")
    public Result<String[]> upload(UploadedFile file, String parentDir) throws Throwable {
        log.info("upload file: {}", file.getName());
        final String[] result = fileUploadService.upload(file, parentDir);
        if (ArrayUtil.isNotEmpty(result)) {
            return Result.succeed(result);
        }
        return Result.failure();
    }


}
