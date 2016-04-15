package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class MainActivity extends BaseActivity  implements ObseverListener {

    @Bind(R.id.btn_to_ManMan)
    Button btn_to_ManMan;
    @Bind(R.id.btn_to_ManComputer)
    Button btn_to_ManComputer;
    @Bind(R.id.btn_to_NetFight)
    Button btn_to_NetFight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bmobconnect();
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
                Toast.makeText(MainActivity.this, status.getMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.btn_to_ManComputer)
    public void onToManComClick(View v){
        startActivity(GameView.class, null, true);
    }

    @OnClick(R.id.btn_to_ManMan)
    public void onToManManClick(View v){
        startActivity(GameView2.class, null, true);
    }

    @OnClick(R.id.btn_to_NetFight)
    public void onToSearchUserClick(View v){
        startActivity(SearchUserActivity.class, null, true);
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
