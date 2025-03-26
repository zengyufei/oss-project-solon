package com.zyf.controller.view;

import cn.hutool.core.util.StrUtil;
import com.zyf.service.JarFileListService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;


/**
 * 接口开发，与控制器开发差不多的; 但进入网关的接口，要用 @Component 注解
 */
@Mapping("view")
@Controller
public class PublicJarFileListViewController {

    @Inject("${files.type}")
    private String filesType;
    @Inject
    private JarFileListService jarFileListService;

    // 文件上传
    @Mapping("/jar/list")
    public ModelAndView list(String dirName) throws Exception {
        ModelAndView model = new ModelAndView(StrUtil.format("{}/jarList.ftl", filesType));
        model.put("title", "jar 版本下载列表页面");
        model.put("list", jarFileListService.getListResult(StrUtil.blankToDefault(dirName, ""), false));
        return model;
    }


}
