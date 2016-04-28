package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import model.UserModel;

/**
 * Created by dz on 2016/4/25.
 */
public class SetPasswordActivity extends BaseActivity {
    @Bind(R.id.et_password)
    EditText et_password;
    @Bind(R.id.et_repassword)
    EditText et_repassword;
    @Bind(R.id.btn_submit)
    Button btn_submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
    }
    @OnClick(R.id.btn_submit)
    public void onSubmit(View v){
        String newpsw = et_password.getText().toString();
        String newrepsw = et_repassword.getText().toString();
        UserModel.getInstance().setPassword(newpsw, newrepsw, new LogInListener() {
            @Override
            public void done(Object o, BmobException e) {
                if (e == null) {
                    //重新登陆
                    BmobIM.getInstance().disConnect();
                    finish();
                    startActivity(LoginActivity.class, null, true);
                } else {
                    toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }
}