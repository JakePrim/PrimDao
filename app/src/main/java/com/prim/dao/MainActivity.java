package com.prim.dao;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.prim.dao.bean.Info;
import com.prim.dao.bean.User;

import java.util.List;

import prim.com.lib_db.db.BaseDao;
import prim.com.lib_db.db.BaseDaoFactory;
import prim.com.lib_db.db.BaseDaoNewImpl;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
