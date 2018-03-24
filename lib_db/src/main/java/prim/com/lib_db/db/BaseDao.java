package prim.com.lib_db.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

    //存储 key - 字段名 value - 成员变量
    private Map<String, Field> cacheMap;

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
            cacheMap = new HashMap<>();
            initCacheMap();
            isInit = true;
        }
        return isInit;
    }

    /**
     * 初始化缓存值
     */
    private void initCacheMap() {
        // 1 获取数据库的所有 列名
        String sql = "select * from " + tbName + " limit 1,0";//空表
        // 拿到所有的列名
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);//获取游标
        String[] columnNames = cursor.getColumnNames();
        //获取所有的成员变量
        Field[] declaredFields = entityClass.getDeclaredFields();
        for (String columnName : columnNames) {
            Field columnField = null;
            for (Field field : declaredFields) {
                String fieldName = null;
                if (field.getAnnotation(DbField.class) != null) {
                    fieldName = field.getAnnotation(DbField.class).value();
                } else {
                    fieldName = field.getName();
                }
                if (fieldName.equals(columnName)) {
                    columnField = field;
                    break;
                }
            }
            if (columnField != null) {
                cacheMap.put(columnName, columnField);
            }
        }
    }

    /**
     * 获取 map 存取 key - 字段名，value - 插入的数据
     *
     * @param entity
     * @return 准备好ContentValues 所需要的数据
     */
    private Map<String, String> getValues(T entity) {
        Map<String, String> map = new HashMap<>();
        // 获取成员变量
        Iterator<Field> iterator = cacheMap.values().iterator();
        while (iterator.hasNext()) {
            Field next = iterator.next();
            next.setAccessible(true);
            //获取成员变量的值
            try {
                Object o = next.get(entity);
                if (o == null) {
                    continue;
                }
                String value = o.toString();
                //获取列名
                String key = null;
                if (next.getAnnotation(DbField.class) != null) {
                    key = next.getAnnotation(DbField.class).value();
                } else {
                    key = next.getName();
                }
                map.put(key, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }


    @Override
    public long insert(T entity) {
        Map<String, String> values = getValues(entity);
        //把数据转移到 ContentValues
        ContentValues contentValues = getContentValues(values);
        long result = sqLiteDatabase.insert(tbName, null, contentValues);
        return result;
    }

    private ContentValues getContentValues(Map<String, String> values) {
        ContentValues contentValues = new ContentValues();
        Set<String> keys = values.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = values.get(key);
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                contentValues.put(key, value);
            }
        }
        return contentValues;
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
}
