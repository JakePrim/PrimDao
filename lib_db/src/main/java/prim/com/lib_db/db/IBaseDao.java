package prim.com.lib_db.db;

/**
 * Created by suful on 2018/3/24.
 */

public interface IBaseDao<T> {
    long insert(T entry);
}
