package com.zyf.service;

import cn.hutool.core.util.StrUtil;
import com.zyf.common.aliyun.FileInfo;
import com.zyf.utils.AliyunUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JarFileListService {

    public List<Map<String, String>> getListResult(String dirName, boolean isAll) {
        log.debug("请求阿里云oss 路径: {}", StrUtil.blankToDefault(dirName, "/"));
        // 指定前缀
        String keyPrefix = StrUtil.blankToDefault(dirName, "");
        List<FileInfo> fileInfos = AliyunUtil.getFileInfos(keyPrefix);
        List<Map<String, String>> fileNames = new ArrayList<>();
        for (FileInfo s : fileInfos) {
            final String fileName = s.getKey();
            if (StrUtil.endWith(fileName, "/")) {
                continue;
            }
            if (!isAll && !StrUtil.endWith(fileName, ".jar")) {
                continue;
            }
            final Map<String, String> map = new HashMap<>();
            map.put("name", fileName);
            map.put("url", AliyunUtil.getUrl() + "?fileName=" + fileName);
            fileNames.add(map);
        }
        return fileNames;
    }

}
