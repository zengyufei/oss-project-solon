package com.zyf.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.zyf.common.aliyun.FileInfo;
import com.zyf.common.enums.FilesType;
import com.zyf.controller.entity.FileShowInfo;
import com.zyf.controller.entity.InputFileList;
import com.zyf.controller.entity.ListContext;
import com.zyf.utils.AliyunUtil;
import com.zyf.utils.LocalUtil;
import com.zyf.utils.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@Component
public class FileListService {
    Db db;
    static final String tableName = "t_short_url";

    @Inject("${files.type}")
    private String filesType;

    @Inject("${files.shortUrl.show-host}")
    private String shortUrlHost;
    @Inject("${files.shortUrl.host}")
    private String host;
    @Inject("${files.shortUrl.port}")
    private String port;
    @Inject("${base.url}")
    private String baseUrl;

    @Init
    public void init() {
        db = Db.use("db");
    }

    /**
     * 获取列表结果
     *
     * @return {@link List}<{@link Map}<{@link String}, {@link String}>>
     */
    public List<FileShowInfo> getListResult(InputFileList inputFileList) throws Exception {
        final Context ctx = Context.current();
        final String ip = ctx.realIp();
        final boolean isPublic = StrUtil.equalsIgnoreCase("public", MsgUtil.getMsg("public"));
        final boolean isPrivate = StrUtil.equalsIgnoreCase("private", MsgUtil.getMsg("private"));
        final boolean isLogin = StpUtil.isLogin();
        final boolean isAll = inputFileList.isAll();
        String dirName = StrUtil.blankToDefault(inputFileList.getDirName(), "/");
        String 身份 = "非法";
        if (isLogin) {
            身份 = "登录用户";
        } else if (isPublic) {
            身份 = "游客";
        } else if (isPrivate) {
            身份 = "秘钥";
        }
        // 指定前缀
        String keyPrefix = StrUtil.blankToDefault(dirName, "");

        if (FilesType.Local.eq(filesType)) {
        log.debug("{} [{}]请求local 路径: {}", ip, 身份, dirName);
            return getLocalFileShowInfos(keyPrefix, isAll);
        } else if (FilesType.Aliyun.eq(filesType)) {
        log.debug("{} [{}]请求阿里云oss 路径: {}", ip, 身份, dirName);
            return getAliyunFileShowInfos(keyPrefix, isAll);
        } else {
            throw new RuntimeException("未指定存储方式！");
        }
    }

    private @NotNull List<FileShowInfo> getLocalFileShowInfos(String keyPrefix, boolean isAll) throws Exception {
        List<FileShowInfo> fileInfos = LocalUtil.getFileInfos(keyPrefix);
        final ListContext listContext = MsgUtil.getMsg("listContext");

        List<FileShowInfo> files = new ArrayList<>();
        for (FileShowInfo fileShowInfo : fileInfos) {
            final String fileName = fileShowInfo.getFileName();
            final String extName = fileShowInfo.getExtName();
            if (StrUtil.endWith(fileName, "/")) {
                continue;
            }
            if (listContext != null) {
                final List<String> ignores = listContext.getIgnores();
                if (CollUtil.isNotEmpty(ignores)) {
                    if (!isAll && StrUtil.endWithAnyIgnoreCase(extName, ignores.toArray(String[]::new))) {
                        continue;
                    }
                }
                final BiFunction<String, String, Boolean> isOpenFileFunc = listContext.getIsOpenFile();
                if (isOpenFileFunc != null) {
                    if (!isAll && !isOpenFileFunc.apply(fileName, extName)) {
                        continue;
                    }
                }
            }
            final String url = LocalUtil.getUrl() + "?fileName=" + fileShowInfo.getFileId();
            fileShowInfo.setUrl(url);

            final Entity record = new Entity(tableName);
            record.set("longUrl", fileShowInfo.getUrl() + "&size=" + fileShowInfo.getSize());
            record.set("baseUrl", baseUrl);
            final Entity entity = db.get(record);
            if (entity != null) {
                fileShowInfo.setShortUrl(StrUtil.format("{}/s/{}", shortUrlHost, entity.getStr("shortUrl")));
                fileShowInfo.setVisit(entity.getLong("visit"));
            }

            files.add(fileShowInfo);
        }
        return files;
    }

    private @NotNull List<FileShowInfo> getAliyunFileShowInfos(String keyPrefix, boolean isAll) throws Exception{
        List<FileInfo> fileInfos = AliyunUtil.getFileInfos(keyPrefix);
        final ListContext listContext = MsgUtil.getMsg("listContext");

        List<FileShowInfo> files = new ArrayList<>();
        for (FileInfo s : fileInfos) {
            final String fileName = s.getKey();
            if (StrUtil.endWith(fileName, "/")) {
                continue;
            }
            if (listContext != null) {
                final List<String> ignores = listContext.getIgnores();
                if (CollUtil.isNotEmpty(ignores)) {
                    if (!isAll && StrUtil.endWithAnyIgnoreCase(fileName, ignores.toArray(String[]::new))) {
                        continue;
                    }
                }
                final BiFunction<String, String, Boolean> isOpenFileFunc = listContext.getIsOpenFile();
                if (isOpenFileFunc != null) {
                    if (!isAll && !isOpenFileFunc.apply(FileNameUtil.mainName(fileName), FileNameUtil.extName(fileName))) {
                        continue;
                    }
                }
            }
            FileShowInfo fileShowInfo = new FileShowInfo();
//            fileShowInfo.setFileId(fileName);
            fileShowInfo.setFileName(fileName);
            fileShowInfo.setLastModified(DateUtil.format(s.getLastModified(), "yyyy-MM-dd HH:mm:ss"));
            fileShowInfo.setUrl(AliyunUtil.getUrl() + "?fileName=" + URLUtil.encode(fileName));
            fileShowInfo.setSize(s.getSize());
            fileShowInfo.setSizeStr(FileUtil.readableFileSize(s.getSize()));

            final Entity record = new Entity(tableName);
            record.set("longUrl", fileShowInfo.getUrl() + "&size=" + fileShowInfo.getSize());
            record.set("baseUrl", baseUrl);
            final Entity entity = db.get(record);
            if (entity != null) {
                fileShowInfo.setShortUrl(StrUtil.format("{}/s/{}", shortUrlHost, entity.getStr("shortUrl")));
                fileShowInfo.setVisit(entity.getLong("visit"));
            }

            files.add(fileShowInfo);
        }
        return files;
    }


}
