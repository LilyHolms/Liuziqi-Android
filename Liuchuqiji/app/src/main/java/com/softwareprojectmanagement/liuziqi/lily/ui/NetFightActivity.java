package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.ChessGridNetAdapter;
import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.ObseverListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.exception.BmobException;
import core.Config;
import entity.NetFightMessage;
import entity.User;
import model.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lily on 16/4/9.
 * 这是一个粗糙的对战版本,可传递落子信息
 * 还没有写轮到谁,暂时写成了本地为黑棋对方为白棋
 * 可传递悔棋信息,暂时写成了只能悔一次棋
 *
 * bug:按home键,程序在后台运行时,收到的消息无法在回到界面时显示
 */
public class NetFightActivity extends BaseActivity  implements ObseverListener {
   // implements ObseverListener是为在锁屏期间使用BmobNotificationManager

    private int KONGNUM = Config.KONGNUM;
    private int BLACKNUM = Config.BLACKNUM;
    private int WHITENUM = Config.WHITENUM;
    private int BOARDSIZE = Config.BOARDSIZE;
    private int screen_width;//屏幕宽度
    private int arr_board[][] = new int[BOARDSIZE][BOARDSIZE];
    private int last_coodinate;//最后一颗棋子的坐标

    @Bind(R.id.btn_withdraw_chess)
    Button btn_withdraw_chess;
    @Bind(R.id.extra_message)
    TextView extra_message;
    @Bind(R.id.my_name)
    TextView my_name;
    @Bind(R.id.opponent_name)
    TextView opponent_name;
    @Bind(R.id.gridview)
    GridView gv_gameView;

    private ChessGridNetAdapter chessGridAdapter;

    BmobIMConversation c;

    protected String title() {
        return c.getConversationTitle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_fight);
        c= BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) getBundle().getSerializable("c"));

        initView();
        myGame();
    }

    void initView() {
        gv_gameView = (GridView) findViewById(R.id.gridview);
        gv_gameView.setNumColumns(BOARDSIZE);

        WindowManager wm = this.getWindowManager();
        screen_width = wm.getDefaultDisplay().getWidth();

        //为GridView设置适配器
        chessGridAdapter = new ChessGridNetAdapter(this,screen_width,arr_board);//lily
        gv_gameView.setAdapter(chessGridAdapter);

        //因为gridview每个小格子的长宽必须是整数,所以设置重新设置一下棋盘的大小
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) gv_gameView.getLayoutParams();
        linearParams.height = (screen_width / BOARDSIZE) * BOARDSIZE;
        linearParams.width = linearParams.height;
        gv_gameView.setLayoutParams(linearParams);

        //注册监听事件
        gv_gameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//落子
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if (arr_board[position / BOARDSIZE][position % BOARDSIZE] == KONGNUM) {
                    toast("落子位置:" + position);
                    arr_board[position / BOARDSIZE][position % BOARDSIZE] = BLACKNUM;//这里暂时写成本方是黑子
                    last_coodinate = position;
                    btn_withdraw_chess.setClickable(true);
                    btn_withdraw_chess.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    chessGridAdapter.notifyDataSetChanged();//更新数据,刷新
                    sentNetFightMessage(position, WHITENUM);//这里暂时写成对方是白子
                }
            }
        });

        //显示玩家本人username
        User user = UserModel.getInstance().getCurrentUser();//UserModel类用到了传说中的单例模式
        my_name.setText(user.getUsername());
        //显示对方username
        opponent_name.setText(c.getConversationTitle());
    }

    //悔棋按钮
    @OnClick(R.id.btn_withdraw_chess)
    public void onWithdrawChessClick(View view){
        //更新本地
        arr_board[last_coodinate / BOARDSIZE][last_coodinate % BOARDSIZE] = KONGNUM;
        chessGridAdapter.notifyDataSetChanged();
        //更新对方
        sentNetFightMessage(last_coodinate, KONGNUM);
        btn_withdraw_chess.setClickable(false);
        btn_withdraw_chess.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
    }

    void myGame(){
        for(int i = 0 ; i < BOARDSIZE ; i ++ ){
            for(int j = 0; j < BOARDSIZE ; j++){
                arr_board[i][j] = KONGNUM;
            }
        }
        arr_board[3][4] = BLACKNUM;
        arr_board[1][2] = WHITENUM;
    }

    /**
     * 发送下棋消息
     * @param coordinate 棋盘上坐标
     * @param chesscolor 棋子颜色黑棋或白棋
     */
    public void sentNetFightMessage(int coordinate, int chesscolor){
        NetFightMessage msg = new NetFightMessage();
        msg.setContent("" + coordinate);
        Map<String,Object> map = new HashMap<>();
        map.put("COLOR", chesscolor);//TODO
        msg.setExtraMap(map);
        c.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                Logger.i("othermsg:" + msg.toString());
                if (e == null) {//发送成功
                    toast("发送成功");
                } else {//发送失败
                    toast("发送失败:" + e.getMessage());
                }
            }
        });
    }

    /**接收下棋消息
     * @param event
     */
    @Subscribe
    public void onEventMainThread(MessageEvent event){
        addChessMessage2Board(event);
    }

    /**添加下棋消息到界面中
     * @param event
     */
    private void addChessMessage2Board(MessageEvent event){
        BmobIMMessage msg =event.getMessage();
        Logger.i("接收到消息：" + msg.getContent());

        if(c!=null && event!=null && c.getConversationId().equals(event.getConversation().getConversationId()) //如果是当前会话的消息
                ){//删掉并且不为暂态消息
            if(chessGridAdapter.findPosition(msg)<0){//如果未添加到界面中

                String content = msg.getContent();//获得消息中的棋子坐标
                int position = Integer.parseInt(content);
                String temp_color = msg.getExtra();//解析json获得棋子颜色
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(temp_color);
                    temp_color = jsonObject.get("COLOR").toString();
                    extra_message.setText(temp_color);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int temp_color_int = Integer.parseInt(temp_color);
                arr_board[position / BOARDSIZE][position % BOARDSIZE] = temp_color_int;
                chessGridAdapter.notifyDataSetChanged();
                //更新该会话下面的已读状态
                c.updateReceiveStatus(msg);
            }
        }else{
            Logger.i("不是与当前聊天对象的消息");
        }
    }

    @Override
    protected void onResume() {
        //锁屏期间的收到的未读消息需要添加到聊天界面中
        addUnReadMessage();
        //添加通知监听
        BmobNotificationManager.getInstance(this).addObserver(this);
        // 有可能锁屏期间，在聊天界面出现通知栏，这时候需要清除通知
        BmobNotificationManager.getInstance(this).cancelNotification();
        super.onResume();
    }
    /**
     * 添加未读的通知栏消息到聊天界面
     */
    private void addUnReadMessage(){
        List<MessageEvent> cache = BmobNotificationManager.getInstance(this).getNotificationCacheList();
        if(cache.size()>0){
            Logger.i("有未读的消息需要添加");
            System.out.println("有未读的消息需要添加");
            int size =cache.size();
            for(int i=0;i<size;i++){
                MessageEvent event = cache.get(i);
                addChessMessage2Board(event);
            }
        }
    }

}