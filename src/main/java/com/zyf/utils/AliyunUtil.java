package com.zyf.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import com.zyf.common.aliyun.FileInfo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.model.Media;
import org.noear.solon.cloud.utils.http.HttpUtils;
import org.noear.solon.core.Props;
import org.noear.solon.core.handle.Result;
import org.w3c.dom.Element;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class AliyunUtil {

    public static Result uploadFile(String key, Media media) throws RuntimeException {
        return uploadFile(null, key, media);
    }

    public static Result uploadFile(String bucket, String key, Media media) throws RuntimeException {

        final Props cfg = Solon.cfg();
        final String endpoint = cfg.get("files.aliyun.endpoint");
        final String accessKey = cfg.get("files.aliyun.accessKey");
        final String secretKey = cfg.get("files.aliyun.secretKey");

        if (Utils.isEmpty(bucket)) {
            bucket = cfg.get("files.aliyun.bucket");
        }

        String streamMime = media.contentType();
        if (Utils.isEmpty(streamMime)) {
            streamMime = "text/plain; charset=utf-8";
        }

        try {
            String date = getGmt();
            String objPath = "/" + bucket + StrUtil.addPrefixIfNot(key, "/");
            String url = endpoint.startsWith(bucket) ? "https://" + endpoint + "/" + key : "https://" + bucket + "." + endpoint + "/" + key;
            String Signature = hmacSha1(buildSignData("PUT", date, objPath, streamMime), secretKey);
            String Authorization = "OSS " + accessKey + ":" + Signature;
            final String host = bucket + "." + endpoint;
            String tmp = HttpUtils.http(url)
                    .header("Host", host)
                    .header("Date", date)
                    .header("Authorization", Authorization).bodyRaw(media.body(), streamMime).put();
            return Result.succeed(tmp);
        } catch (Exception var11) {
            throw new RuntimeException(var11);
        }
    }

    public static Media downloadFile(String key) throws RuntimeException {
        return downloadFile(null, key);
    }

    public static Media downloadFile(String bucket, String key) throws RuntimeException {
        final Props cfg = Solon.cfg();
        final String endpoint = cfg.get("files.aliyun.endpoint");
        final String accessKey = cfg.get("files.aliyun.accessKey");
        final String secretKey = cfg.get("files.aliyun.secretKey");

        if (Utils.isEmpty(bucket)) {
            bucket = cfg.get("files.aliyun.bucket");
        }

        try {
            String date = getGmt();
            String objPath = "/" + bucket + StrUtil.addPrefixIfNot(key, "/");
            String url = endpoint.startsWith(bucket) ? "https://" + endpoint + "/" + key : "https://" + bucket + "." + endpoint + "/" + key;
            final String data = buildSignData("GET", date, objPath, null);
            String Signature = hmacSha1(data, secretKey);
            String Authorization = "OSS " + accessKey + ":" + Signature;
            final String host = bucket + "." + endpoint;
            final Response response = HttpUtils.http(url)
                    .header("Host", host)
                    .header("Date", date)
                    .header("Authorization", Authorization).exec("GET");
            ResponseBody obj = response.body();
            return new Media(obj.byteStream(), obj.contentType().toString(), obj.contentLength());
        } catch (IOException var10) {
            throw new RuntimeException(var10);
        }
    }

    public static List<FileInfo> getFileInfos(String keyPrefix) throws RuntimeException {
        final Props cfg = Solon.cfg();
        final String bucket = cfg.get("files.aliyun.bucket");
        final String endpoint = cfg.get("files.aliyun.endpoint");
        final String accessKey = cfg.get("files.aliyun.accessKey");
        final String secretKey = cfg.get("files.aliyun.secretKey");
        try {
            String date = getGmt();
            String objPath = "/" + bucket + StrUtil.addPrefixIfNot(keyPrefix, "/");
            String url = endpoint.startsWith(bucket) ? "https://" + endpoint + "/" + keyPrefix : "https://" + bucket + "." + endpoint + "/" + keyPrefix;
            final String data = buildSignData("GET", date, objPath, null);
            String Signature = hmacSha1(data, secretKey);
            String Authorization = "OSS " + accessKey + ":" + Signature;
            final String host = bucket + "." + endpoint;
            final ResponseBody body = HttpUtils.http(url)
                    .header("Host", host)
                    .header("Date", date)
                    .header("Authorization", Authorization).exec("get").body();
            return parseListObjects(body.byteStream());
        } catch (IOException var9) {
            throw new RuntimeException();
        }
    }


    public static String getFileSize(String key) throws RuntimeException {
        return getFileSize(null, key);
    }

    public static String getFileSize(String bucket, String key) throws RuntimeException {
        final Props cfg = Solon.cfg();
        final String endpoint = cfg.get("files.aliyun.endpoint");
        final String accessKey = cfg.get("files.aliyun.accessKey");
        final String secretKey = cfg.get("files.aliyun.secretKey");

        if (Utils.isEmpty(bucket)) {
            bucket = cfg.get("files.aliyun.bucket");
        }

        try {
            String date = getGmt();
            String objPath = "/" + bucket + StrUtil.addPrefixIfNot(key, "/");
            String url = endpoint.startsWith(bucket) ? "https://" + endpoint + "/" + key + "?objectMeta" : "https://" + bucket + "." + endpoint + "/" + key + "?objectMeta";
            final String data = buildSignData("HEAD", date, objPath, null);
            String Signature = hmacSha1(data, secretKey);
            String Authorization = "OSS " + accessKey + ":" + Signature;
            final String host = bucket + "." + endpoint;
            final Response response = HttpUtils.http(url)
                    .header("Host", host)
                    .header("Date", date)
                    .header("Authorization", Authorization).exec("HEAD");
            return getFileSize(response);
        } catch (IOException var10) {
            throw new RuntimeException(var10);
        }
    }


    public static Result delFile(String key) {
        return delFile(null, key);
    }

    public static Result delFile(String bucket, String key) {
        final Props cfg = Solon.cfg();
        final String endpoint = cfg.get("files.aliyun.endpoint");
        final String accessKey = cfg.get("files.aliyun.accessKey");
        final String secretKey = cfg.get("files.aliyun.secretKey");

        if (Utils.isEmpty(bucket)) {
            bucket = cfg.get("files.aliyun.bucket");
        }

        try {
            String date = getGmt();
            String objPath = "/" + bucket + StrUtil.addPrefixIfNot(key, "/");
            String url = endpoint.startsWith(bucket) ? "https://" + endpoint + "/" + key : "https://" + bucket + "." + endpoint + "/" + key;
            String Signature = hmacSha1(buildSignData("DELETE", date, objPath, (String) null), secretKey);
            String Authorization = "OSS " + accessKey + ":" + Signature;
            final String host = bucket + "." + endpoint;
            String tmp = HttpUtils.http(url)
                    .header("Host", host)
                    .header("Date", date)
                    .header("Authorization", Authorization).delete();
            return org.noear.solon.core.handle.Result.succeed(tmp);
        } catch (IOException var9) {
            throw new RuntimeException(var9);
        }
    }

    private static String getGmt() {
        return toString(new Date(), "EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US, TimeZone.getTimeZone("GMT"));
    }

    private static List<FileInfo> parseListObjects(InputStream responseBody) {
        try {
            List<FileInfo> list = new ArrayList<>();
            Element root = getXmlRootElement(responseBody);
            if (StrUtil.equalsIgnoreCase(root.getTagName(), "error")) {
                final Element element = XmlUtil.getElementByXPath("//Error/Message", root);
                log.error(XmlUtil.toStr(root));
                log.error(element.getTextContent());
                throw new RuntimeException(element.getTextContent());
            }
            List<Element> objectSummaryElems = XmlUtil.getElements(root, "Contents");

            for (Element elem : objectSummaryElems) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setKey(XmlUtil.elementText(elem, "Key"));
                fileInfo.setLastModified(DateUtil.parse(XmlUtil.elementText(elem, "LastModified")));
                fileInfo.setSize(Long.parseLong(XmlUtil.elementText(elem, "Size")));
                fileInfo.setStorageClass(XmlUtil.elementText(elem, "StorageClass"));
                fileInfo.setType(XmlUtil.elementText(elem, "Type"));
                list.add(fileInfo);
            }
            return list;
        } catch (Exception var9) {
            throw new RuntimeException(var9);
        }
    }


    private static String getFileSize(Response response) {
        final Headers headers = response.headers();
        return headers.get("Content-Length");
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

    private static org.w3c.dom.Element getXmlRootElement(InputStream responseBody) throws Exception {
        final byte[] bytes = IoUtil.readBytes(responseBody);
        final String readUtf8 = IoUtil.readUtf8(IoUtil.toStream(bytes));
        if (log.isTraceEnabled()) {
            log.trace("读取xml内容: " + readUtf8);
        }
        final org.w3c.dom.Document document = XmlUtil.readXML(IoUtil.toStream(bytes));
        return XmlUtil.getRootElement(document);
    }

    private static String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception var6) {
            throw new RuntimeException(var6);
        }
    }

    private static String buildSignData(String method, String date, String objPath, String contentType) {
        return contentType == null ? method + "\n\n\n" + date + "\n" + objPath : method + "\n\n" + contentType + "\n" + date + "\n" + objPath;
    }


    private static String toString(Date date, String format, Locale locale, TimeZone timeZone) {
        DateFormat df;
        if (locale == null) {
            df = new SimpleDateFormat(format);
        } else {
            df = new SimpleDateFormat(format, locale);
        }

        if (timeZone != null) {
            df.setTimeZone(timeZone);
        }

        return df.format(date);
    }
}
