package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import adapter.SearchUserAdapter;
import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.ConversationListener;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import entity.User;
import event.ChatEvent;
import model.BaseModel;
import model.UserModel;

/**
 * Created by Lily on 16/4/6.
 */
public class SearchUserActivity extends BaseActivity {

    @Bind(R.id.et_find_name)
    EditText et_find_name;
    @Bind(R.id.sw_refresh)
    SwipeRefreshLayout sw_refresh;
    @Bind(R.id.btn_search)
    Button btn_search;
    @Bind(R.id.rc_view)
    RecyclerView rc_view;
    LinearLayoutManager layoutManager;

    SearchUserAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
//lily        initNaviView();没用ParentWithNaviActivity里的导航栏
        adapter =new SearchUserAdapter();
        layoutManager = new LinearLayoutManager(this);
        rc_view.setLayoutManager(layoutManager);
        rc_view.setAdapter(adapter);
        sw_refresh.setEnabled(true);
        sw_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                query();
            }
        });
    }

    @OnClick(R.id.btn_search)
    public void onSearchClick(View view){
        sw_refresh.setRefreshing(true);
        query();
    }

    public void query(){
        String name =et_find_name.getText().toString();
        if(TextUtils.isEmpty(name)){
            toast("请填写用户名");
            sw_refresh.setRefreshing(false);
            return;
        }
        UserModel.getInstance().queryUsers(name, BaseModel.DEFAULT_LIMIT, new FindListener<User>() {
            @Override
            public void onSuccess(List<User> list) {
                sw_refresh.setRefreshing(false);
                adapter.setDatas(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(int i, String s) {
                sw_refresh.setRefreshing(false);
                toast(s + "(" + i + ")");
            }
        });
    }

    //开启私聊
    @Subscribe
    public void onEventMainThread(ChatEvent event){
        BmobIMUserInfo info =event.info;
        //如果需要更新用户资料，开发者只需要传新的info进去就可以了
        Logger.i("" + info.getName() + "," + info.getAvatar() + "," + info.getUserId());
        BmobIM.getInstance().startPrivateConversation(info, new ConversationListener() {
            @Override
            public void done(BmobIMConversation c, BmobException e) {
                if (e == null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("c", c);
                    startActivity(NetChatActivity.class, bundle, false);//lily这里设置跳转到的界面
                } else {
                    toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }

}
