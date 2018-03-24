package com.prim.dao.bean;

import prim.com.lib_db.annotation.DbField;
import prim.com.lib_db.annotation.DbTable;

/**
 * Created by suful on 2018/3/24.
 * 如果存在注解 则用注解作为表 或 字段名 如果没有注解 则用当前的命名
 */

@DbTable("tb_user")
public class User {
    @DbField("_id")
    public int id;
    public String name;

    @DbField("u_pass")
    public String pass;
}
