package com.zyf.controller.view;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.util.StrUtil;
import com.zyf.AppConstant;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;


/**
 * 私有文件列表视图控制器
 *
 * @author zyf
 * @date 2024/03/22
 */
@Mapping("view")
@Controller
public class PrivateFileListViewController {

    @Inject("${files.type}")
    private String filesType;
    /**
     * 列表
     *
     * @param dirName dir名称
     * @return {@link ModelAndView}
     * @throws Exception 例外
     */
    @SaCheckRole(AppConstant.ADMIN)
    @Mapping("/private/file/list")
    public ModelAndView list(String dirName) throws Exception {
        ModelAndView model = new ModelAndView(StrUtil.format("{}/privateFile.ftl", filesType));
        model.put("title", "全部文件管理");
        return model;
    }


}
