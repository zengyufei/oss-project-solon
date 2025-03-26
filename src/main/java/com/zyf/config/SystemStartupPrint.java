package com.zyf.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.SolonProps;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SystemStartupPrint implements EventListener<AppLoadEndEvent> {

    private static List<String> getIps() {
        List<String> ipList = new ArrayList<>();
        for (String localIpv4 : NetUtil.localIpv4s()) {
            ipList.add(localIpv4);
        }
        return ipList;
    }

    @Override
    public void onEvent(AppLoadEndEvent event) throws Throwable {

        log.info("启动完毕...");

        ThreadUtil.execute(() -> {
            final SolonProps cfg = Solon.cfg();
            final String appName = cfg.appName();
            final String env = cfg.env();
            final String contextPath = cfg.get("server.contextPath");
            final String port = cfg.get("server.port");

            List<String> ipList = getIps();

            final StringBuilder stringBuilder = new StringBuilder(
                    "\n------------- " + appName + " (" + env + ") 启动成功 --by " + DateUtil.now() + " -------------\n");
            stringBuilder.append("\t主页访问: \n");
            stringBuilder.append("\t\t- 访问: http://")
                    .append("localhost")
                    .append(":")
                    .append(port);
            if (contextPath != null && !contextPath.isEmpty()) {
                stringBuilder.append(contextPath);
            }
            stringBuilder.append("\n");
            for (String ip : ipList) {
                stringBuilder.append("\t\t- 访问: http://")
                        .append(ip)
                        .append(":")
                        .append(port);
                if (contextPath != null && !contextPath.isEmpty()) {
                    stringBuilder.append(contextPath);
                }
                stringBuilder.append("\n");
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println(stringBuilder);
        });
    }

}
