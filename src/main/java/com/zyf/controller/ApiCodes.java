package com.zyf.controller;

import org.noear.solon.annotation.Note;

/**
 * @author noear 2021/6/11 created
 */
public class ApiCodes {
    /**
     * 成功
     */
    @Note("成功")
    public static final ApiCode CODE_200 = new ApiCode(200, "Succeed");

    /**
     * 失败，未知错误
     */
    @Note("失败，未知错误")
    public static final ApiCode CODE_400 = new ApiCode(400, "Unknown error");

    /**
     * 登录失败
     */
    @Note("登录失败")
    public static final ApiCode CODE_315 = new ApiCode(315, "登录失败!");

    /**
     * 请求的接口不存在或不再支持
     */
    @Note("请求的接口不存在或不再支持")
    public static final ApiCode CODE_4001011 = new ApiCode(4001011, "接口/页面不存在");

    /**
     * 请求的签名校验失败
     */
    @Note("请求的签名校验失败")
    public static final ApiCode CODE_4001013 = new ApiCode(4001013, "缺少令牌");

    /**
     * 请求的签名校验失败
     */
    @Note("未登录不能访问")
    public static final ApiCode CODE_4001014 = new ApiCode(4001014, "未登录不能访问");

    /**
     * 文件名不能为空
     */
    @Note("文件名不能为空")
    public static final ApiCode CODE_4001015 = new ApiCode(4001015, "文件名不能为空");

    /**
     * 无权限
     */
    @Note("无权限")
    public static final ApiCode CODE_4001016 = new ApiCode(4001016, "无权限");

}
