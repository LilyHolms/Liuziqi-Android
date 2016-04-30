package com.softwareprojectmanagement.liuziqi.lily.ui;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import com.ant.liao.GifView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.ConversationListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.ValueEventListener;
import core.Config;
import entity.User;
import entity.WhiteChessQueue;
import model.UserModel;

public class NetFightSetActivity extends BaseActivity {

    @Bind(R.id.btn_determine)
    Button btn_determine;

    RadioGroup  choosetype,choosetime;
    private int chess_type,chess_time;


    User user,user_fight;
    WhiteChessQueue player;
    BmobRealTimeData userdata;
    int click_flag,cancel_flag;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_fight_set);
        user  = UserModel.getInstance().getCurrentUser();//获取当前用户
        user_fight = new User();//对方

        click_flag = 1;
        cancel_flag = 0;
        btn_determine=(Button)findViewById(R.id.btn_determine);
        btn_determine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDetermineClick(v);
            }
        });
        chess_type = -1;
        choosetype=(RadioGroup)findViewById(R.id.rg_choosetype);
        choosetype.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if(checkedId== R.id.rbtn_choosewhite){
                    chess_type = Config.WHITENUM;
                    Toast.makeText(NetFightSetActivity.this, "选白", Toast.LENGTH_LONG).show();
                }
                else if(checkedId== R.id.rbtn_chooseblack){
                    chess_type = Config.BLACKNUM;
                    Toast.makeText(NetFightSetActivity.this, "选黑", Toast.LENGTH_LONG).show();
                }

            }
        });
        chess_time = -1;
        choosetime=(RadioGroup)findViewById(R.id.rg_choosetime);
        choosetime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if (checkedId == R.id.rbtn_choosefifteen) {
                    chess_time = 15;
                    Toast.makeText(NetFightSetActivity.this, "15s", Toast.LENGTH_LONG).show();
                } else if (checkedId == R.id.rbtn_choosethirty) {
                    chess_time = 30;
                    Toast.makeText(NetFightSetActivity.this, "30s", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    Thread thread;

    public void onDetermineClick(View view){
        User user = UserModel.getInstance().getCurrentUser();//获取当前用户
       // Toast.makeText(NetFightSetActivity.this, "press", Toast.LENGTH_LONG).show();
        thread = new Thread(new Runnable() {//每点一下新建一个线程
            @Override
            public void run() {

            }
        });
        Logger.i("当前线程进行匹配" + click_flag);
        if (click_flag == 1) {
            click_flag = 0;
            if (chess_type == -1 || chess_time == -1) return;
            if (chess_type == Config.WHITENUM) {
                findUser();
            } else {

                FindMatchUser();
            }
        }
       // thread.start();
        if (chess_type == Config.WHITENUM) {
           // Matchdialog();
        }
    }
    GifView gf1;
    protected  void Matchdialog(){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog,
                 (ViewGroup) findViewById(R.id.dialog));

        gf1 = (GifView) layout.findViewById(R.id.test_gif);
        // 设置背景gif图片资源
        gf1.setGifImage(R.drawable.test_gif);
        //gf1.setOnClickListener(this);
        gf1.setShowDimension(300, 300);
        gf1.setGifImageType(GifView.GifImageType.COVER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("匹配中...").setView(layout);

        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {//取消匹配
                if (player != null)
                    Delete(player);
                click_flag = 1;
                cancel_flag = 1;
                dialog.dismiss();
                //NetFightSetActivity.this.finish();
            }
        });
        builder.show();
    }

    void AddToQueue(){//白棋入队
        //TODO:只能插入一次
        player = new WhiteChessQueue(user.getUsername(),user.getObjectId(),chess_time,"");//第三个参数是否空闲信息
        player.setSysTime(player.getCreatedAt());
        Logger.i("添加数据成功，返回objectId为：" + player.getObjectId() + ",数据在服务端的创建时间为：" +
                player.getCreatedAt());
        player.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Logger.i("添加数据成功，返回objectId为：" + player.getObjectId() + ",数据在服务端的创建时间为：" +
                        player.getCreatedAt());
                Matchdialog();
            }

            @Override
            public void onFailure(int arg0, String arg1) {
                // TODO Auto-generated method stub

            }
        });

    }

    private void FindMatchUser(){//黑棋匹配

        Logger.i("time" + chess_time + "条数据。");
        BmobQuery<WhiteChessQueue> query = new BmobQuery<WhiteChessQueue>();

        query.addWhereEqualTo("chooseTime", chess_time);
        query.setLimit(10);
        query.order("createdAt");/// 根据createdAt字段升序显示数据
        query.findObjects(this, new FindListener<WhiteChessQueue>() {
            @Override
            public void onSuccess(List<WhiteChessQueue> object) {
                // TODO Auto-generated method stub
                Logger.i("查询成功：共" + object.size() + "条数据。");
                //按时间排序
                if (object.size() == 0) {//黑棋
                    Logger.i("Fail:查询成功：共" + object.size());
                    MatchFaildialog();//匹配失败对话框
                }
                for (WhiteChessQueue whiteplayer : object) {

                    Logger.i("查询成功:" + whiteplayer.getObjectId() + " " + whiteplayer.getName());
                    Logger.i("ziji:" + user.getObjectId() + whiteplayer.getUserObjectId2());

                    if (whiteplayer.getUserObjectId2().length() == 0 && whiteplayer.getUserObjectId() != user.getObjectId()) {
                        whiteplayer.setUserObjectId2(user.getObjectId());
                        user_fight.setObjectId(whiteplayer.getUserObjectId());
                        Update(whiteplayer);
                        break;
                    }
                }
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                //Toast.makeText(this, "查询失败：" + msg, Toast.LENGTH_SHORT).show();
                Logger.i("查询失败：" + msg);
            }
        });
    }

    void findUser(){
        BmobQuery<WhiteChessQueue> query = new BmobQuery<WhiteChessQueue>();
        query.addWhereEqualTo("UserObjectId", user.getObjectId());
        query.count(this, WhiteChessQueue.class, new CountListener() {
            @Override
            public void onSuccess(int count) {
                ListenQueue();//多线程
                // TODO Auto-generated method stub
                Logger.i("user count: " + count);
                if (count == 0) {
                    AddToQueue();//多线程
                }
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                //showToast("count failure：" + msg);
            }
        });

    }
    void ListenQueue() {

        userdata = new BmobRealTimeData();
        userdata.start(this, new ValueEventListener() {//连接服务器
            public void onDataChange(JSONObject arg0) {//数据改变回调函数

                if (BmobRealTimeData.ACTION_UPDATETABLE.equals(arg0.optString("action"))) {
                    JSONObject data = arg0.optJSONObject("data");
                    if(cancel_flag==0) {
                        if (data.optString("UserObjectId").equals(user.getObjectId()) && !data.optString("UserObjectId2").isEmpty()) {
                            Logger.i("更新" + data.optString("UserObjectId2"));
                            user_fight.setObjectId(data.optString("UserObjectId2"));
                            Delete(player);
                            MatchSuccess();
                        }
                    }
                }
            }

            public void onConnectCompleted() {

                if (userdata.isConnected()) {

                    Logger.i("连接成功 success");
                    userdata.subTableUpdate("WhiteChessQueue");

                }
            }
        });

        }
    void Delete(WhiteChessQueue p2) {

        p2.delete(this, new DeleteListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Logger.i("删除成功");
            }

            @Override
            public void onFailure(int code, String msg) {
                Logger.i("删除失败");
                // TODO Auto-generated method stub
                //  toast("删除失败：" + msg);
            }
        });
    }
    void Update(WhiteChessQueue p2) {
        Logger.i("WhiteChessQueue p2"+ p2.getUserObjectId2());

        p2.update(this, p2.getObjectId(), new UpdateListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Logger.i("更新成功");
                MatchSuccess();
            }

            @Override
            public void onFailure(int code, String msg) {
                Logger.i("更新失败");
                // TODO Auto-generated method stub
                //  toast("删除失败：" + msg);
            }
        });
    }
    private void MatchSuccess(){

        Logger.i(user.getUsername() + "匹配成功辣" + user_fight.getObjectId());


        BmobQuery<User> query = new BmobQuery<User>();
        query.getObject(this, user_fight.getObjectId(), new GetListener<User>() {

            @Override
            public void onSuccess(User object) {
                // TODO Auto-generated method stub
                //toast("查询成功：");
                user_fight.setUsername(object.getUsername());
                user_fight.setAvatar(object.getAvatar());
                StartCov();
            }

            @Override
            public void onFailure(int code, String arg0) {
                // TODO Auto-generated method stub
                //   toast("查询失败：" + arg0);
            }

        });


    }
    void StartCov(){
        if(chess_type == Config.WHITENUM) {//白棋停止监听
            userdata.unsubTableUpdate("WhiteChessQueue");
        }
        Logger.i("对方信息:" + user_fight.getUsername() + user_fight.getObjectId());
        BmobIMUserInfo info = new BmobIMUserInfo(user_fight.getObjectId(),user_fight.getUsername(),user_fight.getAvatar());

        BmobIM.getInstance().startPrivateConversation(info, new ConversationListener() {
            @Override


            public void done(BmobIMConversation c, BmobException e) {
                if (e == null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("c", c);

                    Intent intent = new Intent(NetFightSetActivity.this, NetFightActivity.class);
                    intent.putExtra("myColor", chess_type);
                    intent.putExtra("waitTime", chess_time);
                    //把bundle也放在Intent中传过去
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);

                } else {
                    //toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
    protected void MatchFaildialog() {//匹配失败对话框

        AlertDialog.Builder builder = new  AlertDialog.Builder(NetFightSetActivity.this);

        builder.setCancelable(false);
        builder.setMessage("无白棋在线、匹配失败");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                click_flag = 1;
                dialog.dismiss();
                //NetFightSetActivity.this.finish();
            }
        });
        builder.create().show();
    }
    protected void ReMatchDialog(){

    }
}
