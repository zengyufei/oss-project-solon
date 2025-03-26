package com.zyf.controller.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InputFileList {

    private String dirName;
    private String orderByField;

    private String orderByType;
    private boolean all;
}
