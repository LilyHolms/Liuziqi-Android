package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.OnClick;
import core.Config;

/**
 * Created by 峻瑶 on 2016/4/23.
 */
public class PVESettingActivity extends BaseActivity {

    @Bind(R.id.pvesetting_btn_assure)
    Button btn_assure;

    @Bind(R.id.pvesetiing_rbtn_chess_black)
    RadioButton rbtn_black;

    @Bind(R.id.pvesetiing_rbtn_chess_white)
    RadioButton rbtn_white;

    @Bind(R.id.pvesetiing_rbtn_level_easy)
    RadioButton rbtn_easy;

    @Bind(R.id.pvesetiing_rbtn_level_middle)
    RadioButton rbtn_middle;

    @Bind(R.id.pvesetiing_rbtn_level_high)
    RadioButton rbtn_high;

    private int peopleColor=-1,AILevel=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pve_setting);

    }
    @OnClick(R.id.pvesetting_btn_assure)
    public void onToGameViewClick(View view)
    {
        if(peopleColor==-1 || AILevel==-1)
        {
            Toast.makeText(PVESettingActivity.this, "请先选择人机设置!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent=new Intent(this,GameView.class);
        intent.putExtra("peopleColor",peopleColor);
        intent.putExtra("AILevel",AILevel);
        startActivity(intent);
    }

    //设置RadioButton监听事件
    @OnClick(R.id.pvesetiing_rbtn_chess_black)
    public void chooseBlackClick(View view)
    {
        peopleColor= Config.BLACKNUM;
    }

    @OnClick(R.id.pvesetiing_rbtn_chess_white)
    public void chooseWhiteClick(View view)
    {
        peopleColor= Config.WHITENUM;
    }

    @OnClick(R.id.pvesetiing_rbtn_level_easy)
    public void chooseEasyClick(View view)
    {
        AILevel=1;
    }

    @OnClick(R.id.pvesetiing_rbtn_level_middle)
    public void chooseMiddleClick(View view)
    {
        AILevel=2;
    }

    @OnClick(R.id.pvesetiing_rbtn_level_high)
    public void chooseHighClick(View view)
    {
        AILevel=3;
    }

}
