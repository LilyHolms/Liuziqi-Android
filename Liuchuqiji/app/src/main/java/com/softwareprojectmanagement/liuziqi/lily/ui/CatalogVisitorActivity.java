package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.OnClick;
//4游客界面
public class CatalogVisitorActivity extends BaseActivity {
    @Bind(R.id.btn_to_login)
    Button btn_to_longin;
    @Bind(R.id.btn_to_register)
    Button btn_to_register;
    @Bind(R.id.btn_to_ManComputer)
    Button btn_to_ManComputer;
    @Bind(R.id.btn_to_ManMan)
    Button btn_to_ManMan;
    @Bind(R.id.btn_to_Rule)
    Button btn_to_Rule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_visitor);
    }

    @OnClick(R.id.btn_to_login)
    public void onToLoginClick(View view){
        startActivity(LoginActivity.class, null, false);
    }

    @OnClick(R.id.btn_to_register)
    public void onToRegisterClick(View view){
        startActivity(RegisterActivity.class, null, false);
    }

    @OnClick(R.id.btn_to_ManComputer)
    public void onToManComputerClick(View view){
        startActivity(PVESettingActivity.class, null, false);
    }

    @OnClick(R.id.btn_to_ManMan)
    public void onToManManClick(View view){
        startActivity(GameView2.class, null, false);
    }

    @OnClick(R.id.btn_to_Rule)
    public void onToRuleClick(View view){
        startActivity(RuleActivity.class, null, false);
    }
}
