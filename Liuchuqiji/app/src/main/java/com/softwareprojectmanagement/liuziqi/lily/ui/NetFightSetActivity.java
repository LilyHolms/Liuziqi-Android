package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
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
    boolean flag;
    BmobRealTimeData userdata;
    int click_cnt ;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_fight_set);
        user  = UserModel.getInstance().getCurrentUser();//获取当前用户
        user_fight = new User();//对方
        flag = false;
        click_cnt = 0;

        ListenQueue();//多线程
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
    public void onDetermineClick(View view){
        if(chess_type==-1||chess_time==-1)return;
        click_cnt++;
        if(click_cnt==1) {
            User user = UserModel.getInstance().getCurrentUser();//获取当前用户
            Toast.makeText(NetFightSetActivity.this, "press", Toast.LENGTH_LONG).show();
            if (chess_type == Config.WHITENUM) {
                findUser();
            } else {
               // QueryToDelete();
                FindMatchUser();
            }
        }

    }

    void AddToQueue(){//白棋入队
        //TODO:只能插入一次
        player = new WhiteChessQueue(user.getUsername(),user.getObjectId(),chess_time,"");//第三个参数是否空闲信息
        player.setSysTime(player.getCreatedAt());
        player.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub

                Logger.i("添加数据成功，返回objectId为：" + player.getObjectId() + ",数据在服务端的创建时间为：" +
                        player.getCreatedAt());
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

//state“free”的数据
        query.addWhereEqualTo("chooseTime", chess_time);
//返回1条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(10);
        query.order("createdAt");/// 根据createdAt字段升序显示数据
//执行查询方法
        query.findObjects(this, new FindListener<WhiteChessQueue>() {
            @Override
            public void onSuccess(List<WhiteChessQueue> object) {
                // TODO Auto-generated method stub
                 Logger.i("查询成功：共" + object.size() + "条数据。");
                //按时间排序

                if (object.size() != 0) {
                    Logger.i("取消监听");
                    userdata.unsubTableUpdate("WhiteChessQueue");
                }
                boolean isfind = false;
                for (WhiteChessQueue whiteplayer : object) {

                    Logger.i("查询成功:" + whiteplayer.getObjectId() + " " + whiteplayer.getName());
                    Logger.i("ziji:" + user.getObjectId() + whiteplayer.getUserObjectId2());

                    if (whiteplayer.getUserObjectId2().length() == 0 && whiteplayer.getUserObjectId() != user.getObjectId()) {
                        whiteplayer.setUserObjectId2(user.getObjectId());
                        user_fight.setObjectId(whiteplayer.getUserObjectId());
                        Update(whiteplayer);
                        isfind = true;
                        break;
                    }
                }
                if(isfind==false){
                    userdata.subTableUpdate("WhiteChessQueue");
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
    void QueryToDelete(){
        BmobQuery<WhiteChessQueue> query = new BmobQuery<WhiteChessQueue>();

//state“free”的数据
        query.addWhereEqualTo("UserObjectId", user.getObjectId());
//返回1条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(10);

//执行查询方法
        query.findObjects(this, new FindListener<WhiteChessQueue>() {
            @Override
            public void onSuccess(List<WhiteChessQueue> object) {
                // TODO Auto-generated method stub
                Logger.i("查询成功：共删除" + object.size() + "条数据。");
                //按时间排序

                for (WhiteChessQueue whiteplayer : object) {
                    Delete(whiteplayer);
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
                    if(chess_type == Config.WHITENUM) {

                        if (data.optString("UserObjectId").equals(user.getObjectId()) && !data.optString("UserObjectId2").isEmpty()) {
                            Logger.i("更新" + data.optString("UserObjectId2"));
                            user_fight.setObjectId(data.optString("UserObjectId2"));
                            Delete(player);
                            MatchSuccess();
                        }
                    }
                    else {
                            if(click_cnt>0) {

                                Logger.i("白棋来了" + data.optString("chooseTime") + data.optString("ObjectId"));
                                if (data.optString("chooseTime").compareTo(String.valueOf(chess_time)) == 0){
                                    Logger.i("时间相等");
                                        if(data.optString("UserObjectId2").length()==0) {
                                            Logger.i("对战对象为空");
                                            Logger.i(data.optString("UserObjectId") + user.getObjectId());
                                            if (data.optString("UserObjectId").compareTo(user.getObjectId()) != 0) {
                                                Logger.i("对战对象不是自己");
                                                FindMatchUser();
                                            }
                                        }
                                }
                            }
                    }
                }
            }

            public void onConnectCompleted() {

                if (userdata.isConnected()) {


                    Logger.i("连接成功 success");
                    //Logger.i("连接成功 success");
                    //userdata.subRowUpdate("WhiteChessQueue", player.getObjectId());
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
                //user_fight.setAvatar(object.getAvatar());
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
       // userdata.unsubTableUpdate("WhiteChessQueue");
        Logger.i("对方信息:" + user_fight.getUsername() + user_fight.getObjectId());
        BmobIMUserInfo info = new BmobIMUserInfo(user_fight.getObjectId(),user_fight.getUsername(),user_fight.getAvatar());

        BmobIM.getInstance().startPrivateConversation(info, new ConversationListener() {
            @Override


            public void done(BmobIMConversation c, BmobException e) {
                if (e == null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("c", c);

                    Intent intent=new Intent(NetFightSetActivity.this,NetFightActivity.class);
                    intent.putExtra("myColor", chess_type);
                    intent.putExtra("waitTime",chess_time);
                    //把bundle也放在Intent中传过去
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                    // startActivity(NetFightActivity.class, bundle, false);//lily这里设置跳转到的界面
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
}
