package prim.com.lib_db.db;

import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;

import prim.com.lib_db.annotation.DbField;
import prim.com.lib_db.annotation.DbTable;

/**
 * Created by suful on 2018/3/24.
 */

public class BaseDao<T> implements IBaseDao<T> {
    private SQLiteDatabase sqLiteDatabase;

    private Class<T> entityClass;

    private String tbName;

    private boolean isInit = false;

    //建表
    protected boolean init(SQLiteDatabase sqLiteDatabase, Class<T> entityClass) {
        this.sqLiteDatabase = sqLiteDatabase;
        this.entityClass = entityClass;
        //1 获取表名
        // 如果该类没有注解 获取类名作为表名
        if (entityClass.getAnnotation(DbTable.class) == null) {
            tbName = entityClass.getSimpleName();
        } else {
            // 该类存在注解 获取注解的value 作为表名
            tbName = entityClass.getAnnotation(DbTable.class).value();
        }
        //建表
        if (!isInit) {
            //判断数据库是否打开
            if (!sqLiteDatabase.isOpen()) {
                return false;
            }
            //数据库已打开
            //建表的sql语句
            sqLiteDatabase.execSQL(getCreateSql());
            isInit = true;
        }
        return isInit;
    }


    @Override
    public long insert(T entity) {
        return 0;
    }

    private String getCreateSql() {
        StringBuilder sqlBuffer = new StringBuilder();
        sqlBuffer.append("create table if not exists ").append(tbName).append(" ( ");
        //反射获取成员变量
        Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Class<?> type = field.getType();//拿到成员变量的类型
            if (field.getAnnotation(DbField.class) == null) {
                // 获取成员变量的名 作为字段名
                String nameField = field.getName();
                if (type == String.class) {
                    sqlBuffer.append(nameField).append(" TEXT,");
                } else if (type == Integer.class) {
                    sqlBuffer.append(nameField).append(" INTEGER,");
                } else if (type == Long.class) {
                    sqlBuffer.append(nameField).append(" BIGINT,");
                } else if (type == Double.class) {
                    sqlBuffer.append(nameField).append(" DOUBLE,");
                } else if (type == byte[].class) {
                    sqlBuffer.append(nameField).append(" BLOB,");
                } else {
                    continue;
                }
            } else {
                // 获取注解的value 作为字段名
                String value = field.getAnnotation(DbField.class).value();
                if (type == String.class) {
                    sqlBuffer.append(value).append(" TEXT,");
                } else if (type == Integer.class) {
                    sqlBuffer.append(value).append(" INTEGER,");
                } else if (type == Long.class) {
                    sqlBuffer.append(value).append(" BIGINT,");
                } else if (type == Double.class) {
                    sqlBuffer.append(value).append(" DOUBLE,");
                } else if (type == byte[].class) {
                    sqlBuffer.append(value).append(" BLOB,");
                } else {
                    continue;
                }
            }
        }
        //注意移除最后一个，号
        if (sqlBuffer.charAt(sqlBuffer.length() - 1) == ',') {
            sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        }
        sqlBuffer.append(" )");
        return sqlBuffer.toString();
    }

    //设置表的字段
    private void setFieldName(String name) {

    }
}
