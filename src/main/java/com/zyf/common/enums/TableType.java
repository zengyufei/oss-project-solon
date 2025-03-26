package com.zyf.common.enums;

import cn.hutool.core.util.StrUtil;

public enum TableType {

    Files("t_files"),

    ShortUrl("t_short_url"),

    ;

    private final String value;

    TableType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean eq(String value) {
        return StrUtil.equalsIgnoreCase(this.value, value);
    }
}
