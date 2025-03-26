package com.zyf.controller.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Data
public class ListContext {

    private List<String> ignores = new ArrayList<>();

    private BiFunction<String, String, Boolean> isOpenFile;

}
