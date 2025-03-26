package com.zyf.controller.apis;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.google.common.hash.Hashing;
import com.zyf.service.caches.FileAllListCacheService;
import com.zyf.service.caches.FileListCacheService;
import com.zyf.utils.Base62Utils;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@Slf4j
@Mapping("api")
@Controller
public class ShortUrlController {

    Db db;
    static final String tableName = "t_short_url";
    static final String sql = "select longUrl, visit from t_short_url where shortUrl = ? and baseUrl = ?";
    @Inject("${base.url}")
    private String baseUrl;

    @Inject("${files.shortUrl.show-host}")
    private String shortUrlHost;
    @Inject("${files.shortUrl.host}")
    private String host;
    @Inject("${files.shortUrl.port}")
    private String port;
    @Inject
    private FileListCacheService fileListCacheService;
    @Inject
    private FileAllListCacheService fileAllListCacheService;

    @Init
    public void init() {
        db = Db.use("db");
    }


    @Post
    @Mapping("/add/shortUrl")
    public Result<String> addShortUrl(Context ctx, String longUrl) throws Exception {
        fileListCacheService.clearAll();
        fileAllListCacheService.clearAll();
        log.info("清除文件缓存");
        final Entity record = new Entity(tableName);
        record.set("longUrl", longUrl);
        record.set("baseUrl", baseUrl);
        final Entity entity = db.get(record);
        if (entity != null) {
            return Result.succeed(StrUtil.format("{}/s/{}", shortUrlHost, entity.getStr("shortUrl")));
        }

        final String shortUrl = generateShortLink(longUrl);

        record.set("shortUrl", shortUrl);
        record.set("baseUrl", baseUrl);
        record.set("visit", 0);
        record.set("createdTime", DateUtil.now());
        record.set("updatedTime", DateUtil.now());
        db.insert(record);

        return Result.succeed(StrUtil.format("{}/s/{}", shortUrlHost, shortUrl));
    }


    @Get
    @Mapping("/shortUrl/{shortUrl}")
    public void shortUrl(Context ctx, String shortUrl) throws Exception {
        final Entity entity = db.queryOne(sql, shortUrl, baseUrl);
        final String longUrl = entity.getStr("longUrl");
        log.info("shortUrl: {} -> {}", shortUrl, longUrl);
        int visit = entity.getInt("visit");
        entity.set("visit", ++visit);
        entity.set("updatedTime", DateUtil.now());
        entity.setTableName(tableName);
        final Entity where = new Entity(tableName);
        where.set("longUrl", longUrl);
        where.set("baseUrl", baseUrl);
        db.update(entity, where);
        // http://www.xunmo.vip:5555/
        // http://oss.xunmo.vip/
        final String format = StrUtil.format("{}:{}{}", host, port, longUrl);
        log.info("redirect: {}", format);
        ctx.redirect(format);
    }

    @Get
    @Mapping("/shortUrl/w/{shortUrl}")
    public void shortUrlWai(Context ctx, String shortUrl) throws Exception {
        final Entity entity = db.queryOne(sql, shortUrl, baseUrl);
        final String longUrl = entity.getStr("longUrl");
        log.info("shortUrl: {} -> {}", shortUrl, longUrl);
        int visit = entity.getInt("visit");
        entity.set("visit", ++visit);
        entity.set("updatedTime", DateUtil.now());
        entity.setTableName(tableName);
        final Entity where = new Entity(tableName);
        where.set("longUrl", longUrl);
        where.set("baseUrl", baseUrl);
        db.update(entity, where);
        // http://www.xunmo.vip:5555/
        // http://oss.xunmo.vip/
        ctx.redirect(longUrl);
    }

    /**
     * 生成短链接
     *
     * @param longLink 长连接
     * @return {@code String}
     */
    public String generateShortLink(String longLink) throws SQLException {
        // 使用 Murmurhash算法，进行哈希，得到长链接Hash值
        long longLinkHash = Hashing.murmur3_32_fixed().hashString(longLink, StandardCharsets.UTF_8).padToLong();
        String shortLink = regenerateOnHashConflict(longLink, longLinkHash);

        // 通过长链接Hash值和长链接检索 (查询数据库里是否唯一)
        final Entity where = new Entity(tableName);
        where.set("shortUrl", shortLink);
        long count = db.count(where);
        while (count > 0) {
            shortLink = regenerateOnHashConflict(longLink, longLinkHash);
            where.set("shortUrl", shortLink);
            count = db.count(where);
        }
        // 如果Hash冲突则加随机盐再次Hash
        return shortLink;
    }


    // 参数1 长连接  参数2 生成的Hash
    private String regenerateOnHashConflict(String longLink, long longLinkHash) {
        // 雪花算法 生成主键id
        long id = IdUtil.getSnowflakeNextId();

        long uniqueIdHash = Hashing.murmur3_32_fixed().hashLong(id).padToLong();
        // 相减主要是为了让哈希值更小
        String shortLink = Base62Utils.encodeToBase62String(Math.abs(longLinkHash - uniqueIdHash));
        System.out.println("产生更短的短连接" + shortLink);

        // SQL 模拟操作 isShortLinkRepeated(短链接) 判定是短链接否唯一
        // SQL ... 如果为false 代表 短链接不存在表中
        boolean isShort = false;
        if (!isShort) {
            // SQL 模拟操作 saveShortLink 保存表中 （shortLink、longLinkHash、longLink）
            // SQL ...
            return shortLink;
        }
        // 如果有 短链接 重复 再走一遍
        return regenerateOnHashConflict(longLink, longLinkHash);
    }
}
