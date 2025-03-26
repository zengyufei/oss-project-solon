package com.zyf.controller.view;

import cn.hutool.core.util.StrUtil;
import com.zyf.service.JarFileListService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;


/**
 * 私有jar文件列表视图控制器
 *
 * @author zyf
 * @date 2024/03/22
 */
@Mapping("view")
@Controller
public class PrivateJarFileListViewController {

    @Inject("${files.type}")
    private String filesType;
    @Inject
    private JarFileListService jarFileListService;

    /**
     * 列表
     *
     * @param dirName dir名称
     * @return {@link ModelAndView}
     * @throws Exception 例外
     */
    @Mapping("/private/jar/list")
    public ModelAndView list(String dirName) throws Exception {
        ModelAndView model = new ModelAndView(StrUtil.format("{}/jarList.ftl", filesType));
        model.put("title", "jar 包下载列表页面");
        model.put("list", jarFileListService.getListResult(StrUtil.blankToDefault(dirName, ""), false));
        return model;
    }


}
