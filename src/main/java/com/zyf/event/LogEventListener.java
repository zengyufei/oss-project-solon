package com.zyf.event;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.event.EventListener;

// 注解模式
@Slf4j
@Component
public class LogEventListener implements EventListener<LogEvent> {
    @Override
    public void onEvent(LogEvent event) throws Throwable {
        log.info(event.getMsg());
    }
}
