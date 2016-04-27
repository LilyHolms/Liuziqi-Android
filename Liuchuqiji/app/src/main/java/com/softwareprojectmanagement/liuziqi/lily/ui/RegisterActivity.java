package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import entity.User;
import model.UserModel;
//2注册
public class RegisterActivity extends BaseActivity {
    @Bind(R.id.et_register_username)
    EditText et_register_username;
    @Bind(R.id.et_register_password)
    EditText et_register_password;
    @Bind(R.id.et_register_repassword)
    EditText et_register_repassword;
    @Bind(R.id.btn_register)
    Button btn_register;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegisterClick();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    private void onRegisterClick() {
        UserModel.getInstance().register(et_register_username.getText().toString(),
                et_register_password.getText().toString(),
                et_register_repassword.getText().toString(),
                new LogInListener() {
                    @Override
                    public void done(Object o, BmobException e) {
                        if (e == null) {
                            User user = (User) o;
                            //更新当前用户资料
                            BmobIM.getInstance().updateUserInfo(new BmobIMUserInfo(user.getObjectId(), user.getUsername(), user.getAvatar()));
                           ///** new 一个Intent对象，并指定要Intent的class */
                           //Intent intent = new Intent();
                           //intent.setClass(RegisterActivity.this, CatalogLoggedActivity.class);
                           ///* 调用一个新的Activity */
                           //startActivity(intent);
                           // /* 关闭原来的 Activity */
                           //RegisterActivity.this.finish();
                            startActivity(CatalogLoggedActivity.class, null, true);
                        } else {
                            toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                        }
                    }
                });


    }
}
