package prim.com.lib_db.sub_qlite;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import prim.com.lib_db.db.BaseDao;
import prim.com.lib_db.db.BaseDaoFactory;

/**
 * Created by suful on 2018/4/3.
 */

public class BaseDaoSubFactory extends BaseDaoFactory {

    private static BaseDaoSubFactory instance;
    private static final String TAG = "BaseDaoFactory";

    public static BaseDaoSubFactory getInstance() {
        if (instance == null) {
            synchronized (BaseDaoSubFactory.class) {
                if (instance == null) {
                    instance = new BaseDaoSubFactory();
                }
            }
        }
        return instance;
    }

    //定义一个用于实现分库的数据库操作对象
    private SQLiteDatabase subSQLiteDatabase;

    private BaseDaoSubFactory() {

    }

    /**
     * 生产basedao 对象
     *
     * @param entityClass
     * @param <T>         User 对象
     * @param <M>         BaseDao {@link BaseDao}
     * @return
     */
    public synchronized <M extends BaseDao<T>, T> M getSubBaseDao(Class<M> daoClass, Class<T> entityClass) {
        BaseDao baseDao = null;
        if (map.get(PrivateDataBaseEnums.database.getValue()) != null) {// 判断当前用户私有的数据库是否存在
            return (M) map.get(PrivateDataBaseEnums.database.getValue());
        }
        // 如果不存在则创建当前用户的私有数据库
        Log.e("jett", "生成数据库文件的位置:" + PrivateDataBaseEnums.database.getValue());
        subSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(PrivateDataBaseEnums.database.getValue(), null);
        try {
            baseDao = daoClass.newInstance();
            baseDao.init(subSQLiteDatabase, entityClass);
            map.put(PrivateDataBaseEnums.database.getValue(), baseDao);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (M) baseDao;
    }
}
