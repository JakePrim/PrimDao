package com.prim.dao;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.prim.dao.bean.User;

import prim.com.lib_db.db.BaseDaoFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseDaoFactory.getInstance().getBaseDao(User.class);
    }
}
