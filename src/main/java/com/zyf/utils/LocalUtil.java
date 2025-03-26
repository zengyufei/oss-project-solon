package com.zyf.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.handler.BeanHandler;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.zyf.common.enums.TableType;
import com.zyf.controller.entity.FileShowInfo;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.cloud.model.Media;
import org.noear.solon.core.Props;
import org.noear.solon.core.handle.Result;
import org.noear.solon.core.handle.UploadedFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class LocalUtil {


    static Db db = Db.use("db");
    static Field[] fields = ReflectUtil.getFields(FileShowInfo.class);

    public static Result uploadFile(String key, UploadedFile file) throws RuntimeException, SQLException {
        return uploadFile(null, key, file);
    }

    public static Result uploadFile(String parentDir, String key, UploadedFile file) throws RuntimeException, SQLException {

        final String fileName = file.getName();
        final long contentSize = file.getContentSize();

        FileShowInfo fileShowInfo = new FileShowInfo();
        fileShowInfo.setFileId(IdUtil.getSnowflakeNextIdStr());
        fileShowInfo.setFileName(FileNameUtil.mainName(fileName));
        fileShowInfo.setRandomName(fileShowInfo.getFileId());
        fileShowInfo.setExtName(FileUtil.extName(fileName));
        fileShowInfo.setFileFullName(fileName);
        fileShowInfo.setLastModified(DateUtil.now());
        fileShowInfo.setUrl(key);
        fileShowInfo.setSize(contentSize);
        fileShowInfo.setSizeStr(FileUtil.readableFileSize(contentSize));
        fileShowInfo.setVisit(0L);
        fileShowInfo.setDir(parentDir);
        fileShowInfo.setDelFlag(0);

        final com.zyf.utils.Media mediaByExtension = com.zyf.utils.Media.getMediaByExtension(fileShowInfo.getExtName());
        String contentType = mediaByExtension.getMimeType();
        fileShowInfo.setContentType(contentType);

        final Props cfg = Solon.cfg();
        String basePath = getBasePath(cfg);

        String fullPath = basePath + File.separator + key;
        if (StrUtil.isNotBlank(parentDir)) {
            fullPath = basePath + File.separator + parentDir + File.separator + key;
            fileShowInfo.setUrl(parentDir + File.separator + key);
        }

        try {
            try (InputStream inputStream = file.getContent();
                 BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fullPath))) {
                byte[] buffer = new byte[1024]; // 创建一个缓冲区
                int bytesRead;

                // 读取 InputStream 并写入文件
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                System.out.println("文件写入成功: " + key);
            }

            return Result.succeed("文件写入成功");
        } catch (Exception var11) {
            throw new RuntimeException(var11);
        } finally {
            FileShowInfo oldFileShowInfo = getByFileNameAndExtName(fileShowInfo.getFileName(), fileShowInfo.getExtName());

            Entity entity = new Entity(TableType.Files.getValue());
            for (Field field : fields) {
                String name = field.getName();
                Object fieldValue = ReflectUtil.getFieldValue(fileShowInfo, field);
                entity.set(name, fieldValue);
            }

            if (oldFileShowInfo == null) {
                try {
                    db.insert(entity);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    db.update(entity, new Entity().set("fileId", oldFileShowInfo.getFileId()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }


        }
    }

    private static FileShowInfo getByFileNameAndExtName(String fileName, String extName) throws SQLException {
        return db.query(StrUtil.format("SELECT * FROM {} WHERE 1=1 AND fileName = ? AND extName = ? AND delFlag = 0",
                TableType.Files.getValue()), BeanHandler.create(FileShowInfo.class), fileName, extName);
    }

    private static String getBasePath(Props cfg) {
        final String windowsPath = cfg.get("files.local.windows.path");
        final String linuxPath = cfg.get("files.local.linux.path");

        String path = windowsPath;
        OsInfo osInfo = SystemUtil.getOsInfo();
        if (osInfo.isLinux()) {
            path = linuxPath;
        }
        return path;
    }

    public static Media downloadFile(String key) throws Exception {
        return downloadFile(null, key);
    }

    public static Media downloadFile(String parentDir, String key) throws Exception {
        FileShowInfo fileShowInfo = getFileShowInfo(key);
        String url = fileShowInfo.getUrl();
        Long size = fileShowInfo.getSize();
//        String fullName = fileShowInfo.getFileName() + StrPool.DOT + fileShowInfo.getExtName();

        final Props cfg = Solon.cfg();
        final String basePath = getBasePath(cfg);

        String fullPath = basePath + File.separator + url;
        if (StrUtil.isNotBlank(parentDir)) {
            fullPath = basePath + File.separator + parentDir + File.separator + url;
            fileShowInfo.setUrl(parentDir + File.separator + url);
        }
        return new Media(IoUtil.toStream(new File(fullPath)), null, size);
    }

    public static FileShowInfo getFileShowInfo(String key) throws SQLException {
        FileShowInfo fileShowInfo = db.query(StrUtil.format("SELECT * FROM {} WHERE 1=1 AND fileId = ? AND delFlag = 0",
                TableType.Files.getValue()), BeanHandler.create(FileShowInfo.class), key);
        return fileShowInfo;
    }

    public static List<FileShowInfo> getFileInfos(String keyPrefix) throws Exception {
        return db.query(StrUtil.format("SELECT * FROM {} WHERE 1=1 AND delFlag = 0", TableType.Files.getValue()), FileShowInfo.class);
    }


    public static Result delFile(String key) throws SQLException {
        return delFile(null, key);
    }

    public static Result delFile(String parentDir, String key) throws SQLException {
        FileShowInfo fileShowInfo = getFileShowInfo(key);

        final Props cfg = Solon.cfg();
        String basePath = getBasePath(cfg);

        String fullPath = basePath + File.separator + fileShowInfo.getUrl();
        if (StrUtil.isNotBlank(parentDir)) {
            fullPath = basePath + File.separator + parentDir + File.separator + fileShowInfo.getUrl();
        }

        delFlagFiles(key);

        FileUtil.del(fullPath);

        return Result.succeed("删除成功！");
    }

    private static void delFlagFiles(String key) throws SQLException {
        Entity entity = new Entity(TableType.Files.getValue());
        entity.set("delFlag", -1);
        db.update(entity, new Entity().set("fileId", key));
    }


    public static String getUrl() {
        final Props cfg = Solon.cfg();
        String baseUrl = cfg.get("base.url");
        baseUrl = StrUtil.prependIfMissing(baseUrl, "/");
        baseUrl = StrUtil.removeSuffix(baseUrl, "/");
        String contextPath = cfg.get("server.contextPath");
        if (StrUtil.isBlank(contextPath)) {
            return baseUrl;
        }
        contextPath = StrUtil.prependIfMissing(contextPath, "/");
        contextPath = StrUtil.removeSuffix(contextPath, "/");
        return contextPath + baseUrl;
    }
}
