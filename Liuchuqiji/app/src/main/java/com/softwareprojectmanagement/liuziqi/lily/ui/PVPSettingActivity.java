package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.OnClick;
import core.Config;

/**
 * Created by 峻瑶 on 2016/4/25.
 */
public class PVPSettingActivity extends BaseActivity {

    @Bind(R.id.pvpsetting_btn_assure)
    Button btn_assure;

    @Bind(R.id.pvpsetiing_rbtn_chess_black)
    RadioButton rbtn_black;

    @Bind(R.id.pvpsetiing_rbtn_chess_white)
    RadioButton rbtn_white;

    @Bind(R.id.pvpsetiing_rbtn_15s)
    RadioButton rbtn_15s;

    @Bind(R.id.pvpsetiing_rbtn_30s)
    RadioButton rbtn_30s;

    private int peopleColor=-1,time=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvp_setting);

    }

    @OnClick(R.id.pvpsetting_btn_assure)
    public void onToGameViewClick(View view)
    {
        if(peopleColor==-1 || time==-1)
        {
            Toast.makeText(PVPSettingActivity.this, "请先完成对弈设置!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent=new Intent(this,GameView.class);
        intent.putExtra("myColor",peopleColor);
        intent.putExtra("waitTime",time);
        startActivity(intent);
    }

    //设置RadioButton监听事件
    @OnClick(R.id.pvpsetiing_rbtn_chess_black)
    public void chooseBlackClick(View view)
    {
        peopleColor= Config.BLACKNUM;
    }

    @OnClick(R.id.pvpsetiing_rbtn_chess_white)
    public void chooseWhiteClick(View view)
    {
        peopleColor= Config.WHITENUM;
    }

    @OnClick(R.id.pvpsetiing_rbtn_15s)
    public void chooseEasyClick(View view)
    {
        time=15;
    }

    @OnClick(R.id.pvpsetiing_rbtn_30s)
    public void chooseMiddleClick(View view)
    {
        time=30;
    }

}
