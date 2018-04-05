package prim.com.lib_db.sub_qlite;

import android.os.Environment;

import java.io.File;

import prim.com.lib_db.db.BaseDaoFactory;

/**
 * Created by suful on 2018/4/3.
 * 用于产生私有数据库的存放位置
 */

public enum PrivateDataBaseEnums {
    database("");
    private String value;

    PrivateDataBaseEnums(String value) {
    }

    // 用于生产路径
    public String getValue() {
        UserDao baseDao = BaseDaoFactory.getInstance().getBaseDao(UserDao.class, User.class);
        User currentUser = baseDao.getCurrentUser();
        if (currentUser != null) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath() + "/n" + currentUser.id + "_login.db";
        }
        return null;
    }
}
