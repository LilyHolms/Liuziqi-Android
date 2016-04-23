package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.OnClick;

//0首页
public class MainActivity extends BaseActivity {
    @Bind(R.id.btn_to_login)
    Button btn_to_longin;
    @Bind(R.id.btn_to_register)
    Button btn_to_register;
    @Bind(R.id.btn_to_visitor)
    Button btn_to_visitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @OnClick(R.id.btn_to_login)
    public void onToLoginClick(View view){
        startActivity(LoginActivity.class, null, true);
    }

    @OnClick(R.id.btn_to_register)
    public void onToRegisterClick(View view){
        startActivity(RegisterActivity.class, null, true);
    }

    @OnClick(R.id.btn_to_visitor)
    public void onToVisitorClick(View view){
        startActivity(CatalogVisitorActivity.class, null, true);
    }
}
