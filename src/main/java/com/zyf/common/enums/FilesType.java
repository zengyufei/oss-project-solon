package com.zyf.common.enums;

import cn.hutool.core.util.StrUtil;

public enum FilesType {

    Local("local"),

    Aliyun("aliyun"),

    ;

    private final String value;

    FilesType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean eq(String value) {
        return StrUtil.equalsIgnoreCase(this.value, value);
    }
}
