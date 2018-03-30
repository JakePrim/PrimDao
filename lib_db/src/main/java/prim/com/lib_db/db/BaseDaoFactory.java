package prim.com.lib_db.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

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

    private BaseDaoFactory() {
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
        try {
            baseDao = daoClass.newInstance();
            baseDao.init(sqLiteDatabase, entityClass);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (M) baseDao;
    }
}
