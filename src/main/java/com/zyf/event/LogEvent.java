package com.zyf.event;

import lombok.Getter;

@Getter
public class LogEvent {
    private String msg;

    public LogEvent(String msg) {
        this.msg = msg;
    }
}
