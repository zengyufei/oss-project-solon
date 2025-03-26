package com.zyf.controller.apis;

import com.zyf.service.FileDelService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;


/**
 * 接口开发，与控制器开发差不多的; 但进入网关的接口，要用 @Component 注解
 */
@Slf4j
@Mapping("api")
@Controller
public class FileDelController {

    @Inject
    private FileDelService fileDelService;

    @NotNull({"fileName"})  // 非NULL验证
    @NotBlank({"fileName"})  // 非Blank验证
    @Mapping("/file/del")
    public Result<Boolean> del(String fileName) throws Exception {
        return Result.succeed(fileDelService.del(fileName));
    }


}
