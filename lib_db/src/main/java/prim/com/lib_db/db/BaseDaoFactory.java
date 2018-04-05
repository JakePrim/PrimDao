package prim.com.lib_db.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by suful on 2018/3/24.
 */

public class BaseDaoFactory {
    private static BaseDaoFactory instance;
    private static final String TAG = "BaseDaoFactory";

    public static BaseDaoFactory getInstance() {
        if (instance == null) {
            synchronized (BaseDaoFactory.class) {
                if (instance == null) {
                    instance = new BaseDaoFactory();
                }
            }
        }
        return instance;
    }

    private SQLiteDatabase sqLiteDatabase;

    private String dbPath;

    // 设计数据库的连接池
    protected Map<String, BaseDao> map = Collections.synchronizedMap(new HashMap<String, BaseDao>());



    protected BaseDaoFactory() {
        // 可以先判断有没有Sd 卡

        // 写到项目中
        dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/lib_prim.db";
        Log.e(TAG, "BaseDaoFactory: " + dbPath);
        //打开或创建数据库
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    /**
     * 生产basedao 对象
     *
     * @param entityClass
     * @param <T>         User 对象
     * @param <M>         BaseDao {@link BaseDao}
     * @return
     */
    public synchronized <M extends BaseDao<T>, T> M getBaseDao(Class<M> daoClass, Class<T> entityClass) {
        BaseDao baseDao = null;
        if (map.get(daoClass.getSimpleName()) != null) {
            return (M) map.get(daoClass.getSimpleName());
        }
        try {
            baseDao = daoClass.newInstance();
            baseDao.init(sqLiteDatabase, entityClass);
            map.put(daoClass.getSimpleName(), baseDao);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (M) baseDao;
    }
}
