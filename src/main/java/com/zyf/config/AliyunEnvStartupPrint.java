package com.zyf.config;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.SolonProps;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Condition;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Condition(onProperty = "${files.type} = aliyun")
public class AliyunEnvStartupPrint implements EventListener<AppLoadEndEvent> {

    @Override
    public void onEvent(AppLoadEndEvent event) throws Throwable {
        final SolonProps cfg = Solon.cfg();
        final String endpoint = cfg.get("files.aliyun.endpoint");
        StringBuilder sb = new StringBuilder("\n当前启动地域: ");
        sb.append(endpoint);
        ThreadUtil.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(sb);
        });
    }
}
