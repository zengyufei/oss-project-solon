package com.zyf.controller.view;

import cn.hutool.core.util.StrUtil;
import com.zyf.utils.ScanUtil;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.validation.annotation.Valid;


@Valid
@Mapping("/view")
@Controller
public class QrScanViewController {

    @Inject("${files.type}")
    private String filesType;

    @Mapping("/qr/scan")
    public ModelAndView qrScan(Context ctx) {
        ModelAndView model = new ModelAndView(StrUtil.format("{}/scan.ftl", filesType));
        String id = ctx.param("id");
        ScanUtil.set(id, "2");
        model.put("id", id);
        return model;
    }


}
