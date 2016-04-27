package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.OnClick;

//2注册
public class RegisterActivity extends AppCompatActivity {
    @Bind(R.id.et_register_username)
    EditText et_register_username;
    @Bind(R.id.et_register_password)
    EditText et_register_password;
    @Bind(R.id.et_register_repassword)
    EditText et_register_repassword;
    @Bind(R.id.btn_register)
    Button btn_register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }


}
