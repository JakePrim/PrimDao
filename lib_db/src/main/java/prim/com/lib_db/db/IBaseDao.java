package prim.com.lib_db.db;

import java.util.List;

/**
 * Created by suful on 2018/3/24.
 */

public interface IBaseDao<T> {
    long insert(T entry);

    long update(T entry, T where);

    int delete(T where);

    List<T> query(T where);

    /**
     * @param where      查询的条件
     * @param orderBy    排序
     * @param startIndex 开始的位置
     * @param limit      查多少个
     * @return
     */
    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);

    List<T> query(String sql);
}
