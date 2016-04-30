package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;

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
}