package com.zyf.test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zyf.controller.entity.InputFileList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TestGuavaCache {

    public static void main(String[] args) throws ExecutionException {
        for (int i = 0; i < 3; i++) {
            final InputFileList inputFileList = new InputFileList()
                    .setAll(true)
                    .setDirName("test")
                    .setOrderByType("asc")
                    .setOrderByField("id");
            final List<Map<String, String>> maps = adminsCache.get(inputFileList);
            System.out.println(maps);
        }
        System.out.println(adminsCache.size());
    }


    static LoadingCache<InputFileList, List<Map<String, String>>> adminsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES) // 缓存项在给定时间内（60min）没有被写访问（创建或覆盖），则回收
            .maximumSize(100) // 最多缓存100项
            .build(new CacheLoader<>() {
                public List<Map<String, String>> load(InputFileList key) throws Exception {
                    final List<Map<String, String>> maps = new ArrayList<>();
                    final HashMap<String, String> map = new HashMap<>();
                    map.put("1", "2");
                    maps.add(map);
                    return maps;
                }
            });

}
