package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import entity.User;
import model.UserModel;

public class LoginActivity extends BaseActivity {

    private EditText et_username;
    private EditText et_password;
    private Button btn_login;
//    private TextView tv_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView(){
        et_username = (EditText)findViewById(R.id.et_username);
        et_password = (EditText)findViewById(R.id.et_password);
        btn_login = (Button)findViewById(R.id.btn_login);
//        tv_register = (TextView)findViewById(R.id.tv_register);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick(v);
            }
        });

//        tv_register.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onRegisterClick(v);
//            }
//        });
    }

    public void onLoginClick(View view){
        UserModel.getInstance().login(et_username.getText().toString(), et_password.getText().toString(), new LogInListener() {

            @Override
            public void done(Object o, BmobException e) {
                if (e == null) {
                    User user =(User)o;
                    //更新当前用户资料
                    BmobIM.getInstance().updateUserInfo(new BmobIMUserInfo(user.getObjectId(),user.getUsername(),user.getAvatar()));
                    startActivity(MainActivity.class, null, true);
                } else {
                    toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }

//    public void onRegisterClick(View view){
//        Toast.makeText(LoginActivity.this, "跳转到RegisterActivity", Toast.LENGTH_SHORT).show();
//
////        startActivity(RegisterActivity.class, null, false);
//    }

//    @Subscribe
//    public void onEventMainThread(FinishEvent event){
//        finish();
//    }
}

