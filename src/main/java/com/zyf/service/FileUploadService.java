package com.zyf.service;

import cn.hutool.core.util.ArrayUtil;
import com.zyf.common.enums.FilesType;
import com.zyf.event.LogEvent;
import com.zyf.service.caches.FileAllListCacheService;
import com.zyf.service.caches.FileListCacheService;
import com.zyf.utils.AliyunUtil;
import com.zyf.utils.CheckUtil;
import com.zyf.utils.LocalUtil;
import com.zyf.utils.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.cloud.model.Media;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.core.handle.UploadedFile;

@Slf4j
@Component
public class FileUploadService {


    @Inject("${files.type}")
    private String filesType;
    @Inject("${files.aliyun.endpoint}")
    private String endpoint;
    @Inject("${files.aliyun.bucket}")
    private String bucket;
    @Inject("${base.url}")
    private String url;
    @Inject("${base.checkToken}")
    private boolean isCheckToken;
    @Inject
    private FileListCacheService fileListCacheService;
    @Inject
    private FileAllListCacheService fileAllListCacheService;

    public String[] upload(UploadedFile file, String parentDir) throws Throwable {
        final Context ctx = Context.current();
        final boolean checked = CheckUtil.checkAuthOrRsa(ctx);
        if (!checked) {
            return new String[]{};
        }

        final String ip = ctx.realIp();
        // 上传媒体
        final String fileName = file.getName();
        final long contentSize = file.getContentSize();
        if (contentSize == 0) {
            return new String[]{};
        }
        EventBus.publish(new LogEvent(ip + " 进行上传, 上传文件[" + fileName + "], 文件大小:" + contentSize));


        // gdj/test.txt
        // gdj/测试.txt
        String pathFileName = PathUtil.getPathFileName(parentDir, fileName);

        if (FilesType.Local.eq(filesType)) {
            final Result result = LocalUtil.uploadFile(pathFileName, file);
            if (result.getCode() == 200) {
                fileListCacheService.clearAll();
                fileAllListCacheService.clearAll();
                log.info("清除文件缓存");

                EventBus.publish(new LogEvent(ip + " 上传文件[" + fileName + "] 成功!"));
                String[] resultUrls = new String[]{
                        PathUtil.getResultUrl(parentDir, fileName),
                        PathUtil.getGetUrl(parentDir, fileName)
                };
                EventBus.publish(new LogEvent("文件[" + fileName + "]请求url:" + ArrayUtil.join(resultUrls, "\n")));
                return resultUrls;
            }
            return new String[]{};
        } else if (FilesType.Aliyun.eq(filesType)) {
            final Result result = AliyunUtil.uploadFile(pathFileName, new Media(file.getContent()));
            if (result.getCode() == 200) {
                fileListCacheService.clearAll();
                fileAllListCacheService.clearAll();
                log.info("清除文件缓存");

                EventBus.publish(new LogEvent(ip + " 上传文件[" + fileName + "] 成功!"));
                String[] resultUrls = new String[]{
                        PathUtil.getResultUrl(parentDir, fileName),
                        PathUtil.getGetUrl(parentDir, fileName)
                };
                EventBus.publish(new LogEvent("文件[" + fileName + "]请求url:" + ArrayUtil.join(resultUrls, "\n")));
                return resultUrls;
            }
            return new String[]{};
        }
        return new String[]{};
    }


}
