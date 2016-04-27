package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.newim.listener.ConnectStatusChangeListener;
import cn.bmob.newim.listener.ObseverListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.exception.BmobException;
import entity.User;
import model.UserModel;
//3-0用户界面
public class CatalogLoggedActivity extends BaseActivity  implements ObseverListener {
    @Bind(R.id.iv_avatar)
    ImageView iv_avatar;
    @Bind(R.id.tv_my_username)
    TextView tv_my_username;
    @Bind(R.id.tv_my_rank)
    TextView tv_my_rank;

    @Bind(R.id.btn_to_logout)
    Button btn_to_logout;

    @Bind(R.id.btn_to_localFight)
    Button btn_to_localFight;
    @Bind(R.id.btn_to_NetFight)
    Button btn_to_NetFight;
    @Bind(R.id.btn_to_Friend)
    Button btn_to_Friend;
    @Bind(R.id.btn_to_PersonalInfor)
    Button btn_to_PersonalInfor;

    @Bind(R.id.btn_to_Rule)
    Button btn_to_Rule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_logged_activity);
        bmobconnect();
        //tv_my_username.setText(UserModel.getInstance().getNickname());
    }
/*-------------------------连接服务器-------------------------------*/
    private void bmobconnect(){
        User user = UserModel.getInstance().getCurrentUser();
        BmobIM.connect(user.getObjectId(), new ConnectListener() {
            @Override
            public void done(String uid, BmobException e) {
                if (e == null) {
                    Logger.i("connect success");
                } else {
                    Logger.e(e.getErrorCode() + "/" + e.getMessage());
                }
            }
        });
        //监听连接状态，也可通过BmobIM.getInstance().getCurrentStatus()来获取当前的长连接状态
        BmobIM.getInstance().setOnConnectStatusChangeListener(new ConnectStatusChangeListener() {
            @Override
            public void onChange(ConnectionStatus status) {
                Toast.makeText(CatalogLoggedActivity.this, status.getMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //登出 未测试
    @OnClick(R.id.btn_to_logout)
    public void onLogoutClick(View view){
        UserModel.getInstance().logout();
        //可断开连接
        BmobIM.getInstance().disConnect();
        finish();
        startActivity(LoginActivity.class, null, true);
    }

    @OnClick(R.id.btn_to_localFight)
    public void onToLocalFightClick(View v){
        startActivity(CatalogLocalFightActivity.class, null, false);
    }
    @OnClick(R.id.btn_to_NetFight)
    public void onToNetFightClick(View v){
        //TODO:需求不太确定,暂时写成跳转到匹配模块
        startActivity(NetMatchingActivity.class, null, false);
    }


    @OnClick(R.id.btn_to_Friend)
    public void onToFriendClick(View v){
        //TODO:应当跳转到好友模块,需求不太确定,暂时写成跳转到搜索用户界面
        startActivity(SearchUserActivity.class, null, false);
    }

    @OnClick(R.id.btn_to_PersonalInfor)
    public void onToPersonalInformationClick(View v){
        startActivity(PersonalInforActivity.class, null, false);
    }

    @OnClick(R.id.btn_to_Rule)
    public void onToRuleClick(View view){
        startActivity(RuleActivity.class, null, false);
    }



    @Override
    protected void onResume() {
        super.onResume();
//        checkRedPoint();
        //添加观察者-用于是否显示通知消息
        BmobNotificationManager.getInstance(this).addObserver(this);
        //进入应用后，通知栏应取消
        BmobNotificationManager.getInstance(this).cancelNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //移除观察者
        BmobNotificationManager.getInstance(this).removeObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清理导致内存泄露的资源
        BmobIM.getInstance().clear();
        //完全退出应用时需调用clearObserver来清除观察者
        BmobNotificationManager.getInstance(this).clearObserver();
    }



}
