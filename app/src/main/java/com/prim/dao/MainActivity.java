package com.prim.dao;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.prim.dao.bean.Info;
import com.prim.dao.bean.Photo;
import com.prim.dao.bean.User;

import java.util.List;

import prim.com.lib_db.db.BaseDao;
import prim.com.lib_db.db.BaseDaoFactory;
import prim.com.lib_db.db.BaseDaoNewImpl;
import prim.com.lib_db.sub_qlite.BaseDaoSubFactory;
import prim.com.lib_db.sub_qlite.UserDao;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userDao = BaseDaoFactory.getInstance().getBaseDao(UserDao.class, prim.com.lib_db.sub_qlite.User.class);
    }

    public void insert(View view) {
        User user = new User();
        user.id = 2;
        user.name = "sss";
        user.pass = "123";
        BaseDaoFactory.getInstance().getBaseDao(BaseDao.class, User.class).insert(user);
    }

    public void delete(View view) {
        User user = new User();
        user.id = 2;
        BaseDaoFactory.getInstance().getBaseDao(BaseDao.class, User.class).delete(user);
    }

    public void update(View view) {
        User user = new User();
        user.name = "bbbbb";
        User whereUser = new User();
        whereUser.id = 2;
        BaseDaoFactory.getInstance().getBaseDao(BaseDao.class, User.class).update(user, whereUser);
    }

    private static final String TAG = "MainActivity";

    public void query(View view) {
        User user = new User();
        user.id = 1;
        List<User> query = BaseDaoFactory.getInstance().getBaseDao(BaseDaoNewImpl.class, User.class).query(user);
        for (int i = 0; i < query.size(); i++) {
            Log.e(TAG, "query: " + query.get(i).toString());
        }
    }

    private int i = 0;
    BaseDao userDao;

    public void clickSubLogin(View view) {
        // 登录以后  服务器返回用户信息
        prim.com.lib_db.sub_qlite.User user = new prim.com.lib_db.sub_qlite.User();
        user.id = i;
        user.name = "张三" + (++i);
        user.pass = "123";
        // 在公共信息中插入到公共数据库中
        userDao.insert(user);
    }

    public void clickSubInster(View view) {
        // 插入私有数据库
        Photo photo = new Photo();
        photo.path = "data/data/img.jpg";
        photo.time = "2018-4-5";

        // 插入到当前登录的用户的私有数据库中
        PhotoDao subBaseDao = BaseDaoSubFactory.getInstance().getSubBaseDao(PhotoDao.class, Photo.class);
        subBaseDao.insert(photo);
    }
}
