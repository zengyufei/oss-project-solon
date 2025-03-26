package com.zyf.service;

import com.zyf.common.enums.FilesType;
import com.zyf.service.caches.FileAllListCacheService;
import com.zyf.service.caches.FileListCacheService;
import com.zyf.utils.AliyunUtil;
import com.zyf.utils.LocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.sql.SQLException;

@Slf4j
@Component
public class FileDelService {

    @Inject("${files.type}")
    private String filesType;
    @Inject("${files.aliyun.endpoint}")
    private String endpoint;
    @Inject("${files.aliyun.bucket}")
    private String bucket;
    @Inject("${files.aliyun.accessKey}")
    private String accessKey;
    @Inject("${files.aliyun.secretKey}")
    private String secretKey;
    @Inject
    private FileListCacheService fileListCacheService;
    @Inject
    private FileAllListCacheService fileAllListCacheService;

    public Boolean del(String fileName) throws SQLException {
        log.debug("删除文件: {}", fileName);
        // 创建OSSClient实例。
        try {
            // 删除文件或目录。如果要删除目录，目录必须为空。
            if (FilesType.Local.eq(filesType)) {
                LocalUtil.delFile(fileName);
            } else if (FilesType.Aliyun.eq(filesType)) {
                AliyunUtil.delFile(fileName);
            } else {
                return false;
            }
            return true;
        } finally {
            fileListCacheService.clearAll();
            fileAllListCacheService.clearAll();
            log.info("清除文件缓存");
        }
    }


}
