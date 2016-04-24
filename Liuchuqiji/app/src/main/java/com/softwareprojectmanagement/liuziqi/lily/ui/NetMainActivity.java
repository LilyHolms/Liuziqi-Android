package com.softwareprojectmanagement.liuziqi.lily.ui;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import adapter.TabPageAdapter;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.ObseverListener;

/**
 * Created by Lily on 16/4/6.
 */

public class NetMainActivity extends BaseFragmentActivity implements
        OnPageChangeListener, OnCheckedChangeListener, ObseverListener{
//add ObseverListener by lily
    private ViewPager viewPager_net_main;
    private TabPageAdapter adapter;
    private List<Fragment> fragments;
    private RadioGroup rgroup_net_tab;
    private RadioButton rbtn_net_tab0, rbtn_net_tab1;

    BmobIMConversation c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_main);
//lily创建聊天回话实例
        c= BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) getBundle().getSerializable("c"));

        fragments = new ArrayList<Fragment>();
        fragments.add(new NetFightFragment());
        fragments.add(new NetChatFragment());

//lily 将聊天回话实例用bundle包装,由activityfragment传递给子fragment
//这里偷懒把fragment个数写死了
        Bundle bundle = new Bundle();
        bundle.putSerializable("c", c);
        fragments.get(0).setArguments(bundle);
        fragments.get(1).setArguments(bundle);


        viewPager_net_main = (ViewPager) findViewById(R.id.viewPager_net_main);
        adapter = new TabPageAdapter(getSupportFragmentManager(), fragments);
        viewPager_net_main.setAdapter(adapter);
        viewPager_net_main.setOffscreenPageLimit(fragments.size() - 1);
        viewPager_net_main.setOnPageChangeListener(this);

        rgroup_net_tab = (RadioGroup) findViewById(R.id.rgroup_net_tab);
        rbtn_net_tab0 = (RadioButton) findViewById(R.id.rbtn_net_tab0);
        rbtn_net_tab1 = (RadioButton) findViewById(R.id.rbtn_net_tab1);
        rgroup_net_tab.setOnCheckedChangeListener(this);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
        Log.v("asdf", "onPageScrollStateChanged");
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        // Log.v("asdf", "onPageScrolled");
    }

    @Override
    public void onPageSelected(int arg0) {
        // TODO Auto-generated method stub
        Log.v("asdf", "onPageSelected");
        getTabState(arg0);

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        switch (checkedId) {
            case R.id.rbtn_net_tab0:
                viewPager_net_main.setCurrentItem(0);
                break;
            case R.id.rbtn_net_tab1:
                viewPager_net_main.setCurrentItem(1);
                break;
            default:
                break;
        }
    }

    private void getTabState(int index) {
        // TODO Auto-generated method stub
        rbtn_net_tab0.setChecked(false);
        rbtn_net_tab1.setChecked(false);

        switch (index) {
            case 0:
                rbtn_net_tab0.setChecked(true);
                break;
            case 1:
                rbtn_net_tab1.setChecked(true);
                break;
            default:
                break;
        }

    }



}