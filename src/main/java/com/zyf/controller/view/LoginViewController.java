package com.zyf.controller.view;

import cn.hutool.core.util.StrUtil;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.validation.annotation.Valid;


@Valid
@Mapping("/view")
@Controller
public class LoginViewController {

    @Inject("${files.type}")
    private String filesType;

    @Mapping("/login")
    public ModelAndView index() {
        ModelAndView model = new ModelAndView(StrUtil.format("{}/login.ftl", filesType));
        model.put("title", "用户登录页");
        return model;
    }


}
