package com.zyf.controller.entity;

import lombok.Data;


@Data
public class FileShowInfo {

    /** 文件id */
    private String fileId;
    /** 文件名 */
    private String fileName;
    /** 文件名 */
    private String fileFullName;
    /** 随机文件名 */
    private String randomName;
    /** 后缀名 */
    private String extName;
    /** 文件类型 */
    private String contentType;
    /** 最后修改时间 */
    private String lastModified;
    /** 下载地址 */
    private String url;
    /** 文件夹 */
    private String dir;
    /** 文件大小 */
    private Long size;
    /** 文件大小字符串 */
    private String sizeStr;
    /** 短链接 */
    private String shortUrl;
    /** 下载次数 */
    private Long visit;
    /** 删除标识 */
    private Integer delFlag;

}
