package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.OnClick;

//3-1用户界面选本地后跳转
public class CatalogLocalFightActivity extends BaseActivity {
    @Bind(R.id.btn_to_ManComputer)
    Button btn_to_ManComputer;
    @Bind(R.id.btn_to_ManMan)
    Button btn_to_ManMan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_local_fight);
    }

    @OnClick(R.id.btn_to_ManComputer)
    public void onToManComputerClick(View view){
        startActivity(GameView.class, null, false);
    }

    @OnClick(R.id.btn_to_ManMan)
    public void onToManManClick(View view){
        startActivity(GameView2.class, null, false);
    }

}
