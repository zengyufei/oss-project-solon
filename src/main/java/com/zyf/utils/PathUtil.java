package com.zyf.utils;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import org.noear.solon.Solon;

public class PathUtil {

    public static String getGetUrl(String parentDir, String fileName) {
        // 获取值
        String url = Solon.cfg().get("base.url");
        boolean isCheckToken = Solon.cfg().getBool("base.checkToken", false);
        // http://120.76.138.68:5555/api/download?
        String resultUrl = StrUtil.appendIfMissing(url, "?");
        // %A8%E9%81%93%E4%BA%A4%E9%80%9A.txt
        // gdj/%A8%E9%81%93%E4%BA%A4%E9%80%9A.txt
        String urlPathFileName = getUrlPathFileName(parentDir, fileName);
        // http://120.76.138.68:5555/api/download?fileName=gdj/%A8%E9%81%93%E4%BA%A4%E9%80%9A.txt
        resultUrl = resultUrl + "fileName=" + urlPathFileName;
//        if (isCheckToken) {
//            // http://120.76.138.68:5555/api/download?fileName=gdj/%A8%E9%81%93%E4%BA%A4%E9%80%9A.txt&t=xm
//            resultUrl = resultUrl + "&t=xm";
//        }
        return resultUrl;
    }

    public static String getResultUrl(String parentDir, String fileName) {
        String url = Solon.cfg().get("base.url") + "File";
        boolean isCheckToken = Solon.cfg().getBool("base.checkToken", false);
        // http://120.76.138.68:5555/api/download/
        String resultUrl = StrUtil.appendIfMissing(url, "/");
        // http://120.76.138.68:5555/api/download/%A8%E9%81%93%E4%BA%A4%E9%80%9A.txt
        // http://120.76.138.68:5555/api/download/test.txt
        if (ReUtil.contains("[^\\x00-\\xff]", fileName)) {
            resultUrl = resultUrl + URLUtil.encode(fileName);
        } else {
            resultUrl = resultUrl + fileName;
        }
        if (StrUtil.isNotBlank(parentDir)) {
            // http://120.76.138.68:5555/api/download/%A8%E9%81%93%E4%BA%A4%E9%80%9A.txt?parentDir=gdj/
            // http://120.76.138.68:5555/api/download/test.txt?parentDir=gdj/
            resultUrl = resultUrl + "?parentDir=" + StrUtil.appendIfMissing(parentDir, "/");
//            if (isCheckToken) {
//                // http://120.76.138.68:5555/api/download/%A8%E9%81%93%E4%BA%A4%E9%80%9A.txt?parentDir=gdj/&t=xm
//                // http://120.76.138.68:5555/api/download/test.txt?parentDir=gdj/&t=xm
//                resultUrl = resultUrl + "&t=xm";
//            }
        }
//        else  if (isCheckToken) {
//            // http://120.76.138.68:5555/api/download/%A8%E9%81%93%E4%BA%A4%E9%80%9A.txt?t=xm
//            // http://120.76.138.68:5555/api/download/test.txt?t=xm
//            resultUrl = resultUrl + "?t=xm";
//        }
        return resultUrl;
    }

    public static String getUrlPathFileName(String parentDir, String fileName) {
        String newFileName = fileName;
        // test.txt
        // %A8%E9%81%93%E4%BA%A4%E9%80%9A.txt
        if (ReUtil.contains("[^\\x00-\\xff]", fileName)) {
            newFileName = URLUtil.encode(fileName);
        }
        // gdj/test.txt
        // gdj/%A8%E9%81%93%E4%BA%A4%E9%80%9A.txt
        if (StrUtil.isNotBlank(parentDir)) {
            newFileName = StrUtil.appendIfMissing(parentDir, "/") + newFileName;
        }
        return newFileName;
    }


    public static String getPathFileName(String parentDir, String fileName) {
        // gdj/test.txt
        // gdj/测试.txt
        String uploadPathFileName = fileName;
        if (StrUtil.isNotBlank(parentDir)) {
            uploadPathFileName = StrUtil.appendIfMissing(parentDir, "/") + fileName;
        }
        return uploadPathFileName;
    }
}
