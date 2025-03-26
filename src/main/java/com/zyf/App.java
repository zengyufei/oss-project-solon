package com.zyf;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Console;
import cn.hutool.db.Db;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.zyf.common.enums.TableType;
import com.zyf.config.rateLimit.RateLimit;
import org.noear.solon.Solon;
import org.noear.solon.core.FactoryManager;
import org.noear.solon.web.cors.CrossFilter;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * -XX:+AllowEnhancedClassRedefinition -javaagent:./HotSecondsServer/HotSecondsServer.jar=hotconf=./HotSecondsServer/hot-seconds-remote.xml --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/jdk.internal.loader=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.desktop/java.beans=ALL-UNNAMED --add-opens java.desktop/com.sun.beans=ALL-UNNAMED --add-opens java.desktop/com.sun.beans.introspect=ALL-UNNAMED --add-opens java.desktop/com.sun.beans.util=ALL-UNNAMED --add-opens java.base/sun.security.action=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED
 *
 * @author zyf
 * @date 2024/06/04
 */
public class App {
    public static void main(String[] args) {
        // fix: https://gitee.com/smartboot/smart-http/issues/I9AQ86
        System.setProperty("smarthttp.log.level", "OFF");

        new Thread(() -> {
            final Map<String, String> map = new LinkedHashMap<>();
            map.put(TableType.ShortUrl.getValue(), "sql/CreateShortUrlTable.sql");
            map.put(TableType.Files.getValue(), "sql/CreateFilesTable.sql");
            final String dbFilePath = "db/oss.sqlite";
            createIfNotExistsSqliteDbFile(dbFilePath, map);
        }).start();

        FactoryManager.getGlobal().threadLocalFactory((applyFor, inheritance0) -> new TransmittableThreadLocal<>());

        Solon.start(App.class, args, app -> {
            final RateLimit.RateLimitInterceptor rateLimitInterceptor = new RateLimit.RateLimitInterceptor();
            app.routerInterceptor(900, rateLimitInterceptor);
            app.context().beanInterceptorAdd(RateLimit.class, rateLimitInterceptor);

            // 例：或者：增加全局处理（用过滤器模式）
            app.filter(-1, new CrossFilter().allowedOrigins("*")); // 加-1 优先级更高

            app.get("/", ctx -> {
                // ctx.forward("/railway-bureau-test/index.html");
                ctx.redirect("/view/login");
//				ctx.render("主页");
            });
            app.get("/connection", ctx -> {
                ctx.render("success");
            });
            app.get("/realIp", ctx -> {
                ctx.render(ctx.realIp());
            });
            app.get("/ip", ctx -> {
                ctx.render(ctx.realIp());
            });

            // 启用 WebSocket 服务
            app.enableWebSocket(true);

            // 将拦截器注册到容器 // 120 为顺序位，不加默认为0
//            Solon.context().beanAroundAdd(CheckLogin.class, new CheckLoginInterceptor(), 120);
        });

    }

    private static void createIfNotExistsSqliteDbFile(String dbFilePath, Map<String, String> tableBySqlFileMap) {
        final File dbFile = new File(FileUtil.getAbsolutePath(new File(dbFilePath)));
        // final String absolutePath = FileUtil.getAbsolutePath(dbFile);
        if (!FileUtil.exist(dbFile)) {
            FileUtil.touch(dbFile);
        }
        if (FileUtil.exist(dbFile)) {
            System.out.println("dbFile:" + dbFile.getAbsolutePath());

            final Db db = Db.use("db");

            for (Map.Entry<String, String> entry : tableBySqlFileMap.entrySet()) {
                String tableName = entry.getKey();
                String sqlFilePath = entry.getValue();

                try {
                    // db.execute("drop table if exists " + tableName);
                    boolean isExists = isExistsTable(db, tableName);
                    if (!isExists) {
                        final String sql = readSqlFile(sqlFilePath);
                        db.execute(sql);
                        isExists = isExistsTable(db, tableName);
                        if (isExists) {
                            Console.log("{} 表创建成功!!", tableName);
                        } else {
                            Console.error("{} 表创建失败!!", tableName);
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }


        }
    }

    private static String readSqlFile(String tableFile) {
        final String sql = new ClassPathResource(tableFile).readUtf8Str();
        Console.log("建表语句: {}", sql);
        return sql;
    }

    private static boolean isExistsTable(Db db, String tableName) throws SQLException {
        Number number = db.queryNumber("SELECT count(1) FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
        boolean isExists = number.intValue() > 0;
        return isExists;
    }
}
