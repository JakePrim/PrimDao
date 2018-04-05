package prim.com.lib_db.sub_qlite;

import android.util.Log;

import java.util.List;

import prim.com.lib_db.db.BaseDao;

/**
 * Created by suful on 2018/4/3.
 * 用于维护公有数据
 */

public class UserDao extends BaseDao<User> {

    private static final String TAG = "UserDao";

    @Override
    public long insert(User entity) {
        // 查到表中的所有的用户记录
        List<User> userList = query(new User());
        User where;
        for (User user : userList) {
            // 将所有的用户的登陆状态 改为 0
            where = new User();
            where.id = user.id;
            user.state = "0";
            Log.e(TAG, "用户: " + user.name + " 未登录");
            update(user, where);
        }
        Log.e(TAG, "用户: " + entity.name + " 登录");
        // 将当前用户的状态给为 1 然后插入
        entity.state = "1";
        return super.insert(entity);
    }

    /**
     * 得到当前登录的user
     */

    public User getCurrentUser() {
        User user = new User();
        user.state = "1";
        List<User> query = query(user);
        if (query != null && query.size() > 0) {
            return query.get(0);
        }
        return null;
    }
}
