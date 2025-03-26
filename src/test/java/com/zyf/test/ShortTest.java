package com.zyf.test;

import com.zyf.App;
import org.junit.jupiter.api.Test;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonTest;

import java.io.IOException;

//通过 SolonTest 可启动开发的服务并进行测试
@SolonTest(value = App.class, args = "--server.port=9001")
public class ShortTest extends HttpTester {

    @Test
    public void demo1_run0() throws IOException {
        //HttpTestBase 提供的请求本地 http 服务的接口
        final String result = path("/api/shortUrl/add")
                .data("longUrl", "http://www.xunmo.vip:5555/api/file/download?fileName=CheckLimitFilter.java&size=4293")
                .post();
        System.out.println(result);
    }

    @Test
    public void demo1_run1() throws IOException {
        //HttpTestBase 提供的请求本地 http 服务的接口
        final String result = path("/api/shortUrl/DXEAAz")
                .get();
        System.out.println(result);
    }

}
