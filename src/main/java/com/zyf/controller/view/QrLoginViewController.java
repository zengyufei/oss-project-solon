package com.zyf.controller.view;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zyf.utils.QrCodeUtil;
import com.zyf.utils.ScanUtil;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.validation.annotation.Valid;

import java.net.URI;


@Valid
@Mapping("/view")
@Controller
public class QrLoginViewController {

    @Inject("${files.type}")
    private String filesType;

    @Mapping("/qr/login")
    public ModelAndView qrLogin(Context ctx) {
        ModelAndView model = new ModelAndView(StrUtil.format("{}/qrLogin.ftl",filesType));
        String id = IdUtil.fastSimpleUUID();
        ScanUtil.set(id, "0");
        // IpUtils 为获取本机ip的工具类，本机测试时，如果用127.0.0.1, localhost那么app扫码访问会有问题哦
        final URI uri = ctx.uri();
        final String host = uri.getHost();
        final int port = uri.getPort();

        // "ws://127.0.0.1:8082/ws/demo/admin"
        model.put("subscribe", StrUtil.format("ws://{}:{}/ws/demo?id={}", host, port, id));

        // 将二维码转换为Base64编码
        final String url = StrUtil.format("http://192.168.2.108:{}/api/scan?id={}", port, id);
        String base64QRCode = java.util.Base64.getEncoder().encodeToString(QrCodeUtil.createQrCode(url));
        model.put("qrcode", "data:image/png;base64," + base64QRCode);
        return model;
    }


}
