package com.zyf.controller.apis;

import com.zyf.controller.entity.FileShowInfo;
import com.zyf.controller.entity.InputFileList;
import com.zyf.controller.entity.ListContext;
import com.zyf.service.FileListService;
import com.zyf.service.JarFileListService;
import com.zyf.service.caches.FileListCacheService;
import com.zyf.utils.MsgUtil;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Result;

import java.util.List;


/**
 * 接口开发，与控制器开发差不多的; 但进入网关的接口，要用 @Component 注解
 */
@Mapping("api")
@Controller
public class JarFileListController {

    @Inject
    private JarFileListService jarFileListService;
    @Inject
    private FileListService fileListService;
    @Inject
    private FileListCacheService fileListCacheService;

    // 文件上传
    @Mapping("/jar/list")
    public Result<List<FileShowInfo>> list(InputFileList inputFileList) throws Exception {
        final ListContext listContext = new ListContext();
        listContext.getIgnores().add(".jar");
        MsgUtil.setMsg("listContext", listContext);
        return Result.succeed(fileListService.getListResult(inputFileList));
    }


}
