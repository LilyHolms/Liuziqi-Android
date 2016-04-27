package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.OnClick;
//9修改个人信息
public class PersonalInforActivity extends BaseActivity {
    @Bind(R.id.btn_to_password)
    Button btn_to_password;
    @Bind(R.id.et_nickname)
    EditText et_nickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_infor);

    }
    @OnClick(R.id.btn_to_password)
    public void onToPasswordClick(View v){
        startActivity(SetPasswordActivity.class,null,true);
    }
}
