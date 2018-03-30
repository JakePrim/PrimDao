package prim.com.lib_db.db;

import java.util.List;

/**
 * Created by suful on 2018/3/24.
 * 进行新的实现 在basedao的基础上实现一个新的
 */

public class BaseDaoNewImpl<T> extends BaseDao<T> {
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit, T groupBy) {
        return null;
    }

    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit, T groupBy, String having) {
        return null;
    }
}
