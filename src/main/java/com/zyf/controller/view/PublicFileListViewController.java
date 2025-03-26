package com.zyf.controller.view;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.StrUtil;
import com.zyf.controller.entity.InputFileList;
import com.zyf.controller.entity.ListContext;
import com.zyf.service.caches.FileListCacheService;
import com.zyf.utils.CheckUtil;
import com.zyf.utils.MsgUtil;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;


/**
 * 公共文件列表视图控制器
 *
 * @author zyf
 * @date 2024/03/22
 */
@Mapping("/view")
@Controller
public class PublicFileListViewController {

    @Inject("${files.type}")
    private String filesType;
    @Inject
    private FileListCacheService fileListCacheService;

    /**
     * 列表
     *
     * @return {@link ModelAndView}
     * @throws Exception 例外
     */
    @SaIgnore
    @Mapping("/file/list")
    public ModelAndView list(InputFileList inputFileList) throws Exception {
        MsgUtil.setMsg("public","public");
        final ListContext listContext = new ListContext();
        listContext.getIgnores().add(".jar");
        listContext.setIsOpenFile(CheckUtil::isOpenFile);
        MsgUtil.setMsg("listContext", listContext);

        ModelAndView model = new ModelAndView(StrUtil.format("{}/fileList.ftl",filesType));
        model.put("title", "公共文件管理");
        inputFileList.setDirName(StrUtil.blankToDefault(inputFileList.getDirName(), "/"));
        inputFileList.setOrderByField("1");
        inputFileList.setOrderByType("1");
        model.put("list", fileListCacheService.getValue(inputFileList));
        return model;
    }


}
