package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;

import entity.User;
import model.UserModel;

/**
 * 启动引导界面
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler =new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                User user = UserModel.getInstance().getCurrentUser();
                if (user == null) {
                    startActivity(MainActivity.class, null, true);
                } else {
                    startActivity(CatalogLoggedActivity.class, null, true);
                }
            }
        }, 1000);

    }

}
