package com.zyf.test;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;

import java.sql.SQLException;

public class DbTest {

    public static void main(String[] args) throws SQLException {

        final Db db = Db.use("db");

        final String tableName = "t_short_url";
        // db.execute("drop table if exists " + tableName);
        Number number = db.queryNumber("SELECT count(1) FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
        System.out.println(number.intValue());


    }

}
