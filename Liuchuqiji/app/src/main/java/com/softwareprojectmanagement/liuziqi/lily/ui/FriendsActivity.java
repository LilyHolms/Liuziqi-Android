package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import adapter.SearchUserAdapter;
import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.ConversationListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import entity.Friends;
import entity.User;
import event.ChatEvent;
import model.UserModel;

/**
 * note1:SwipeRefreshLayout第一次加载页面时不会自动刷新内容
 *       解决办法:http://www.zhihu.com/question/35422150/answer/62695696
 * note2:调用bmob sdk的数据库查询函数,bmob会使多条查询语句多线程执行,
 *       如果查询B要用到查询A的结果,需要加锁.
 */
public class FriendsActivity extends BaseActivity {
    @Bind(R.id.to_Search)
    Button to_Search;
    @Bind(R.id.sw_refresh)
    SwipeRefreshLayout sw_refresh;
    @Bind(R.id.rc_view)
    RecyclerView rc_view;
    LinearLayoutManager layoutManager;
    SearchUserAdapter adapter;//这里简单复用了搜索模块
    String myname;//玩家本人username
    List<String> list_Allf;//我的所有好友的username
    @Bind(R.id.ll_friends)
    LinearLayout ll_friends;
    @Bind(R.id.iv_back)
    ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        myname =  UserModel.getInstance().getCurrentUser().getUsername();

        adapter =new SearchUserAdapter();
        layoutManager = new LinearLayoutManager(this);
        rc_view.setLayoutManager(layoutManager);
        rc_view.setAdapter(adapter);
        sw_refresh.setEnabled(true);


        sw_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sw_refresh.setRefreshing(true);
                queryFriendname();
            }
        });

        list_Allf = new ArrayList<String>();
        sw_refresh.setProgressViewOffset(false, 0, 30);
//        sw_refresh.setRefreshing(true);
//        query();

//        ll_friends.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                ll_friends.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                sw_refresh.setRefreshing(true);
//                //自动刷新
//                query();
//            }
//        });
    }

    @OnClick(R.id.to_Search)
    public void onToSearchClick(View v){
        startActivity(SearchUserActivity.class, null, false);
    }

    @OnClick(R.id.iv_back)
    public void click_to_back(){
        onBackPressed();
    }
    //查询好友名字
    public void queryFriendname(){
        BmobQuery<Friends> list_f1 = new BmobQuery<Friends>();
        list_f1.addWhereEqualTo("id", myname);
        list_f1.setLimit(50);//返回50条数据，如果不加上这条语句，默认返回10条数据

        BmobQuery<Friends> list_f2=new BmobQuery<>();
        list_f2.addWhereEqualTo("friendId",myname);
        list_f2.setLimit(50);//返回50条数据，如果不加上这条语句，默认返回10条数据

        List<BmobQuery<Friends>> queries = new ArrayList<BmobQuery<Friends>>();
        queries.add(list_f1);
        queries.add(list_f2);

        BmobQuery<Friends> mainQuery = new BmobQuery<Friends>();
        mainQuery.or(queries);
        mainQuery.findObjects(this, new FindListener<Friends>() {
            @Override
            public void onSuccess(List<Friends> object) {
                // TODO Auto-generated method stub
                System.out.println("好友表查询到" + object.size()+"条数据");
                for (int i = 0; i < object.size(); i++) {
                    if(object.get(i).getFriendId().equals(myname) ){
                        list_Allf.add(object.get(i).getId());
                    }else{
                        list_Allf.add(object.get(i).getFriendId());
                    }
                }
                queryUser();
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                toast("好友表查询1失败：" + msg);
            }
        });

    }

    //查询用户表
    public void queryUser(){
        BmobQuery<User> list_fUser = new BmobQuery<User>();
        list_fUser.addWhereContainedIn("username", list_Allf);
        list_fUser.setLimit(100);
        list_fUser.findObjects(this, new FindListener<User>() {
            @Override
            public void onSuccess(List<User> object) {
                // TODO Auto-generated method stub
                System.out.println("用户表查询到" + object.size() +"条数据");
                sw_refresh.setRefreshing(false);
                adapter.setDatas(object);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                sw_refresh.setRefreshing(false);
                toast("用户表查询失败：" + msg);
            }
        });
    }

    //开启私聊
    @Subscribe
    public void onEventMainThread(ChatEvent event) {
        adapter.notifyDataSetChanged();
        BmobIMUserInfo info = event.info;
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

    @Override
    public void onResume() {
        super.onResume();
        sw_refresh.setRefreshing(true);
        queryFriendname();
    }
}
