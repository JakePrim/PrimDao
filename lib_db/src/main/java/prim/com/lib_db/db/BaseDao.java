package prim.com.lib_db.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    private static final String TAG = "BaseDao";

    //建表
    public boolean init(SQLiteDatabase sqLiteDatabase, Class<T> entityClass) {
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
        Log.e(TAG, "init: " + isInit + " | isOpen:" + sqLiteDatabase.isOpen());
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


    @Override
    public long insert(T entity) {
        Map<String, String> values = getValues(entity);
        //把数据转移到 ContentValues
        ContentValues contentValues = getContentValues(values);
        return sqLiteDatabase.insert(tbName, null, contentValues);
    }

    @Override
    public long update(T entry, T where) {
//        sqLiteDatabase.update("表名","ContentValues","name = ?",new String[]{}) 原始操作
        int result = -1;
        Map<String, String> values = getValues(entry);// 拿到
        ContentValues contentValues = getContentValues(values);
        Map<String, String> whereCause = getValues(where);
        Codition codition = new Codition(whereCause);
        result = sqLiteDatabase.update(tbName, contentValues, codition.whereCause, codition.whereArgs);
        return result;
    }

    @Override
    public int delete(T where) {
        // 原始的写法
//        sqLiteDatabase.delete("table","where","whereArgs");
        int result;
        Codition codition = new Codition(getValues(where));
        result = sqLiteDatabase.delete(tbName, codition.whereCause, codition.whereArgs);
        return result;
    }

    @Override
    public List<T> query(T where) {
//        sqLiteDatabase.query()
        return query(where, null, null, null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
//        sqLiteDatabase.query(tbName,null,"id = ?",new String[],null,null,orderBy,"1,5")
        String strLimit = null;
        if (startIndex != null && limit != null) {
            strLimit = startIndex + " , " + limit;
        }
        Codition codition = new Codition(getValues(where));
        Cursor query = sqLiteDatabase.query(tbName, null,
                codition.whereCause, codition.whereArgs, null, null, orderBy, strLimit);
        //定义一个用来解析游标的方法
        return getListResult(query, where);
    }

    @Override
    public List<T> query(String sql) {
        return null;
    }

    // obj 是用来表示user的结构的
    private List<T> getListResult(Cursor cursor, T obj) {
        List list = new ArrayList<>();// 存储 user对象
        Object item = null;// Object 的对象
        while (cursor.moveToNext()) {
            try {
                item = obj.getClass().newInstance();// new User 但是不知道Object 中的成员变量
                Iterator<Map.Entry<String, Field>> iterator = cacheMap.entrySet().iterator();//cacheMap 中存的是列名和成员变量
                while (iterator.hasNext()) {// 迭代 map 将成员变量和值 放到item中去 循环完成list 添加一个
                    Map.Entry<String, Field> entry = iterator.next();
                    //取列名
                    String colnumName = entry.getKey();
                    //然后以列名拿到列名在游标的位置
                    Integer columnIndex = cursor.getColumnIndex(colnumName);
                    Field field = entry.getValue();// 拿到列名对应的成员变量
                    Class<?> type = field.getType();//成员变量的类型
                    if (columnIndex != -1) {// 查询成功的
                        if (type == String.class) {
                            //将成员变量还有值 设置到item中去
                            field.set(item, cursor.getString(columnIndex));
                        } else if (type == Integer.class) {
                            field.set(item, cursor.getInt(columnIndex));
                        } else if (type == Long.class) {
                            field.set(item, cursor.getLong(columnIndex));
                        } else if (type == Double.class) {
                            field.set(item, cursor.getDouble(columnIndex));
                        } else if (type == byte[].class) {
                            field.set(item, cursor.getBlob(columnIndex));
                        } else if (type == int.class) {
                            field.set(item, cursor.getInt(columnIndex));
                        } else {
                            continue;
                        }
                    }
                }
                list.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        cursor.close();//关闭游标
        return list;
    }

    /**
     * 条件 拼接处理类
     */
    private class Codition {
        private String whereCause;//name = ?
        private String[] whereArgs;//new String[]{}

        Codition(Map<String, String> whereCasue) {
            this(whereCasue, true);
        }

        Codition(Map<String, String> whereCasue, boolean isAnd) {
            ArrayList<String> valueList = new ArrayList<>();
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append("1==1");
            Set<String> keySet = whereCasue.keySet();
            for (String key : keySet) {
                String value = whereCasue.get(key);
                if (value != null) {
                    // or
                    if (isAnd) {
                        keyBuilder.append(" and ").append(key).append("=?");
                    } else {
                        keyBuilder.append(" or ").append(key).append("=?");
                    }
                    valueList.add(value);
                }
            }
            this.whereCause = keyBuilder.toString();
            this.whereArgs = valueList.toArray(new String[valueList.size()]);
        }
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
     * @return 准备好ContentValues 所需要的数据 拿到所有的字段名 和 值
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

    /**
     * 通过{@link #getValues(Object)} 将数据转移到ContentValues
     *
     * @param values
     * @return ContentValues
     */
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

    /**
     * 创建表的sql语句
     *
     * @return sql语句
     */
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
                } else if (type == int.class) {
                    sqlBuffer.append(nameField).append(" INTEGER,");
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
                } else if (type == int.class) {
                    sqlBuffer.append(value).append(" INTEGER,");
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
