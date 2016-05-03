package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ant.liao.GifView;
import com.orhanobut.logger.Logger;
import com.softwareprojectmanagement.liuziqi.jelly.move;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.ChessGridNetAdapter;
import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.ObseverListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
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
    private int BLACKLAST=Config.BLACKLAST;
    private int WHITELAST=Config.WHITELAST;
    private int SELECTPOS=Config.SELECTPOS;
    private int LOSE=-10;
    private int screen_height;
    int screen_width;//屏幕宽度
    private int arr_board[][] = new int[BOARDSIZE][BOARDSIZE];
    private int last_coodinate;//最后一颗棋子的坐标

//    @Bind(R.id.btn_withdraw_chess)
//    Button btn_withdraw_chess;
//    @Bind(R.id.extra_message)
//    TextView extra_message;
//    @Bind(R.id.my_name)
//    TextView my_name;
//    @Bind(R.id.opponent_name)

    TextView opponent_name;

    @Bind(R.id.gridview)
    GridView gv_gameView;
    @Bind(R.id.blackPhoto)
    ImageView blackPhoto;
    @Bind(R.id.whitePhoto)
    ImageView whitePhoto;
    @Bind(R.id.blackGrade)
    TextView blackGrade;
    @Bind(R.id.whiteGrade)
    TextView whiteGrade;

    private ChessGridNetAdapter chessGridAdapter;

    BmobIMConversation c;
    private User oppo_User;//对手User

    private int chessSum=0;//下了第几个棋，每方走两个棋子
    private int getSum=0;  //收到了几个棋子
    private int dir[][]={{1,0},{1,1},{0,1},{1,-1}};
    private int playColor=BLACKNUM;
    private int myColor;
    private boolean isGameover=false;
    private boolean isLose=false;

    //双方的总计时器和单步倒计时器
    private Chronometer whiteTimer,whiteStepTimer;
    private Chronometer blackTimer,blackStepTimer;

    //单步倒计时时间
    private int whiteStep,blackStep;
    int stepTime=30;

    //记录上一步双方招法用于悔棋
    private move lastBlack=new move();
    private move lastWhite=new move();

    private Button btn_return;
    private Button btn_chat;
    private Button btn_lose;
    private Button btn_move;

    //记录当前第几手
    private TextView view_steps;
    int steps=1;

    //显示双方ID
    private TextView view_blackID;
    private TextView view_whiteID;

    //是否有选择框存在
    private boolean select=false;
    private double selX,selY;   //当前鼠标位置
    private int posX,posY;      //当前选中位置
    private double itemSize;    //格子的宽高
    private double downX,downY; //上次鼠标位置

    protected String title() {
        return c.getConversationTitle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.include_board);
        //c= BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) getBundle().getSerializable("c"));
        Intent intent=this.getIntent();
        Bundle mybundle=intent.getBundleExtra("bundle");
        c=BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) mybundle.getSerializable("c"));
        myColor=intent.getIntExtra("myColor", 1);
        stepTime=intent.getIntExtra("waitTime",30);
        queryOppUser();
        initView();
        myGame();
    }

    public void queryOppUser(){
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username", c.getConversationTitle());
        //执行查询方法
        query.findObjects(this, new FindListener<User>() {
            @Override
            public void onSuccess(List<User> object) {
                // TODO Auto-generated method stub
//                toast("对手信息查询成功：共" + object.size() + "条数据。");
                if (object.size() > 0) {
                    oppo_User = object.get(0);
                    init_user_infor();
                }
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                toast("查询失败：" + msg);
            }
        });
    }
    public void init_user_infor(){
        if(myColor==BLACKNUM){//如果本人执黑子
            view_blackID.setText(UserModel.getInstance().getCurrentUser().getUsername());
            blackGrade.setText("积分:" + UserModel.getInstance().getPoints());
            UserModel.getInstance().loadAvatar(this, blackPhoto);

            view_whiteID.setText(c.getConversationTitle());
            whiteGrade.setText("积分:" + oppo_User.getPoints());
            oppo_User.loadAvatar(this, whitePhoto);
        }else{//如果本人执白子
            view_whiteID.setText(UserModel.getInstance().getCurrentUser().getUsername());
            whiteGrade.setText("积分:"+UserModel.getInstance().getPoints());
            UserModel.getInstance().loadAvatar(this, whitePhoto);

            view_blackID.setText(c.getConversationTitle());
            blackGrade.setText("积分:"+oppo_User.getPoints());
            oppo_User.loadAvatar(this, blackPhoto);
        }
    }

    void initView() {
        gv_gameView = (GridView) findViewById(R.id.gridview);
        gv_gameView.setNumColumns(BOARDSIZE);

        WindowManager wm = this.getWindowManager();
        screen_width = wm.getDefaultDisplay().getWidth();
        screen_height=wm.getDefaultDisplay().getHeight();


        //因为gridview每个小格子的长宽必须是整数,所以设置重新设置一下棋盘的大小
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) gv_gameView.getLayoutParams();

        linearParams.height = screen_height*525/1000;
        linearParams.width = linearParams.height;
        itemSize=linearParams.height / BOARDSIZE;

        gv_gameView.setLayoutParams(linearParams);
        //为GridView设置适配器
        chessGridAdapter = new ChessGridNetAdapter(this, linearParams.width,arr_board);//lily
        gv_gameView.setAdapter(chessGridAdapter);

        view_steps=(TextView)this.findViewById(R.id.text_playturn);
        view_blackID=(TextView)this.findViewById(R.id.blackRes);
        view_whiteID=(TextView)this.findViewById(R.id.whiteRes);

//        //显示玩家本人username
//        User user = UserModel.getInstance().getCurrentUser();//UserModel类用到了传说中的单例模式
//        view_blackID.setText(user.getUsername());
//        //显示对方username
//        view_whiteID.setText(c.getConversationTitle());





//        //注册监听事件
//        gv_gameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//落子
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//
//                if (isGameover || playColor != myColor)
//                    return;
//                int nowX, nowY;
//                nowX = position / BOARDSIZE;
//                nowY = position % BOARDSIZE;
//                if (arr_board[nowX][nowY] == KONGNUM) {
//                    chessSum++;
//
//                    //根据当前玩家颜色来落对应的子
//                    if (playColor == BLACKNUM) {
//                        arr_board[nowX][nowY] = BLACKLAST;
//                        lastBlack.x[chessSum - 1] = nowX;
//                        lastBlack.y[chessSum - 1] = nowY;
//                        lastBlack.len = chessSum;
//                        if (chessSum == 2) {
//                            arr_board[lastBlack.x[0]][lastBlack.y[0]] = BLACKLAST;
//                        }
//                        sentNetFightMessage(position, BLACKLAST);
//                    } else if (playColor == WHITENUM) {
//                        arr_board[nowX][nowY] = WHITELAST;
//                        lastWhite.x[chessSum - 1] = nowX;
//                        lastWhite.y[chessSum - 1] = nowY;
//                        lastWhite.len = chessSum;
//                        if (chessSum == 2) {
//                            arr_board[lastWhite.x[0]][lastWhite.y[0]] = WHITELAST;
//                        }
//                        sentNetFightMessage(position, WHITELAST);
//                    }
//                    toast("落子位置:" + position);
////                    arr_board[position / BOARDSIZE][position % BOARDSIZE] = BLACKNUM;//这里暂时写成本方是黑子
////                    last_coodinate = position;
////                    btn_withdraw_chess.setClickable(true);
////                    btn_withdraw_chess.setBackgroundColor(getResources().getColor(R.color.colorAccent));
//                    chessGridAdapter.notifyDataSetChanged();//更新数据,刷新
//                    //sentNetFightMessage(position, WHITENUM);//这里暂时写成对方是白子
//
//                    //变色
//                    if (steps == 1 && playColor == BLACKNUM && chessSum == 1) {
//
//                        changeTimer(playColor);
//                        playColor ^= 3;
//                        chessSum = 0;
//                        steps++;
//                        view_steps.setText("第" + steps + "手");
//                    } else if (chessSum == 2) {
//                        chessSum = 0;
//                        changeTimer(playColor);
//                        playColor ^= 3;
//                        steps++;
//                        view_steps.setText("第" + steps + "手");
//                    }
//                    //判断胜负
//                    if (checkWin(nowX, nowY)) {
//                        //drawGameRes(playColor ^ 3);
//                        if (arr_board[nowX][nowY] == WHITENUM) {
//                            Toast.makeText(NetFightActivity.this, "游戏结束！白方获胜！", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(NetFightActivity.this, "游戏结束！黑方获胜！", Toast.LENGTH_SHORT).show();
//                        }
//                    } else if (chechDraw()) {
//                        //drawGameRes(KONGNUM);
//                        Toast.makeText(NetFightActivity.this, "游戏结束！平局！", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });

        //绑定各个按钮事件，本地游戏隐藏聊按钮
        btn_return=(Button)this.findViewById(R.id.btn_return);
        btn_chat=(Button)this.findViewById(R.id.btn_chat);
        btn_lose=(Button)this.findViewById(R.id.btn_lose);

        //悔棋按钮事件
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGameover && lastBlack.len > 0 && lastWhite.len > 0 && steps > 2) {
                    if (chessSum == 1) {
                        if (playColor == WHITENUM) {
                            arr_board[lastWhite.x[0]][lastWhite.y[0]] = KONGNUM;
                            sentNetFightMessage(lastWhite.x[0]*9+lastWhite.y[0],KONGNUM);
                            lastWhite.len = 0;

                        } else {
                            arr_board[lastBlack.x[0]][lastBlack.y[0]] = KONGNUM;
                            sentNetFightMessage(lastBlack.x[0]*9+lastBlack.y[0],KONGNUM);
                            lastBlack.len = 0;
                        }

                        chessGridAdapter.notifyDataSetChanged();
                        chessSum = 0;
                    }
                }
            }
        });

        //认输按钮事件
        btn_lose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGameover) {
                    isLose=true;
                    drawGameRes(myColor^3);
                    isGameover=true;
                    sentNetFightMessage(0,LOSE);
                }
            }
        });

        //绑定选择框移动事件
        gv_gameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        downX = event.getY();
                        downY = event.getX();
                        if (select == false) {
                            selX = downX;
                            selY = downY;
                            select = true;
                            //显示选择框
                            posX = (int) (selX / itemSize);
                            posY = (int) (selY / itemSize);
                            if (inBoard(posX, posY)) {
                                arr_board[posX][posY] += SELECTPOS;
                                chessGridAdapter.notifyDataSetChanged();
                            }
                        } else {
                            return false;
                        }
                    }
                    case MotionEvent.ACTION_MOVE: {
                        selX += (event.getY() - downX);
                        selY += (event.getX() - downY);
                        downX = event.getY();
                        downY = event.getX();
                        int nowPosX = (int) (selX / itemSize);
                        int nowPosY = (int) (selY / itemSize);
                        if (posX == nowPosX && posY == nowPosY) {
                            return false;
                        } else if (inBoard(nowPosX, nowPosY)) {
                            arr_board[posX][posY] -= SELECTPOS;
                            arr_board[nowPosX][nowPosY] += SELECTPOS;
                            posX = nowPosX;
                            posY = nowPosY;
                            chessGridAdapter.notifyDataSetChanged();
                        } else {
                            //边界处理，越界归位
                            selX = posX * itemSize;
                            selY = posY * itemSize;
                            downX = event.getY();
                            downY = event.getX();
                        }
                    }
                    default:
                        break;
                }
                return false;
            }
        });

        btn_move=(Button)this.findViewById(R.id.btn_move);
        btn_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGameover || playColor != myColor)
                    return;
                int nowX, nowY;
                nowX = posX;
                nowY = posY;
                int position=nowX*BOARDSIZE+posY;
                if (arr_board[nowX][nowY]%9 == KONGNUM) {
                    chessSum++;

                    //根据当前玩家颜色来落对应的子
                    if (playColor == BLACKNUM) {
                        arr_board[nowX][nowY] = BLACKLAST;
                        lastBlack.x[chessSum - 1] = nowX;
                        lastBlack.y[chessSum - 1] = nowY;
                        lastBlack.len = chessSum;
                        if (chessSum == 1) {
                            for (int i = 0; i < lastWhite.len; i++) {
                                arr_board[lastWhite.x[i]][lastWhite.y[i]] = WHITENUM;
                            }
                        }
                        sentNetFightMessage(position, BLACKLAST);
                    } else if (playColor == WHITENUM) {
                        arr_board[nowX][nowY] = WHITELAST;
                        lastWhite.x[chessSum - 1] = nowX;
                        lastWhite.y[chessSum - 1] = nowY;
                        lastWhite.len = chessSum;
                        if (chessSum == 1) {
                            for (int i = 0; i < lastBlack.len; i++) {
                                arr_board[lastBlack.x[i]][lastBlack.y[i]] = BLACKNUM;
                            }
                        }
                        sentNetFightMessage(position, WHITELAST);
                    }
                    toast("落子位置:" + position);
//                    arr_board[position / BOARDSIZE][position % BOARDSIZE] = BLACKNUM;//这里暂时写成本方是黑子
//                    last_coodinate = position;
//                    btn_withdraw_chess.setClickable(true);
//                    btn_withdraw_chess.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    chessGridAdapter.notifyDataSetChanged();//更新数据,刷新
                    //sentNetFightMessage(position, WHITENUM);//这里暂时写成对方是白子

                    //变色
                    if (steps == 1 && playColor == BLACKNUM && chessSum == 1) {

                        changeTimer(playColor);
                        playColor ^= 3;
                        chessSum = 0;
                        steps++;
                        view_steps.setText("第" + steps + "手");
                    } else if (chessSum == 2) {
                        chessSum = 0;
                        changeTimer(playColor);
                        playColor ^= 3;
                        steps++;
                        view_steps.setText("第" + steps + "手");
                    }
                    select=false;
                    //判断胜负
                    if (checkWin(nowX, nowY)) {
                        drawGameRes(myColor);
                        if (arr_board[nowX][nowY] == myColor) {
                            Toast.makeText(NetFightActivity.this, "获胜！", Toast.LENGTH_SHORT).show();
                            //TODO:弹出win.jpg并可按返回按钮关闭dialog

                        } else {
                            Toast.makeText(NetFightActivity.this, "失败！", Toast.LENGTH_SHORT).show();
                            //TODO:弹出lose.jpg并可按返回按钮关闭dialog
                        }

                    } else if (chechDraw()) {
                        drawGameRes(KONGNUM);
                        Toast.makeText(NetFightActivity.this, "平局！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

//    //悔棋按钮
//    @OnClick(R.id.btn_withdraw_chess)
//    public void onWithdrawChessClick(View view){
//        //更新本地
//        arr_board[last_coodinate / BOARDSIZE][last_coodinate % BOARDSIZE] = KONGNUM;
//        chessGridAdapter.notifyDataSetChanged();
//        //更新对方
//        sentNetFightMessage(last_coodinate, KONGNUM);
//        btn_withdraw_chess.setClickable(false);
//        btn_withdraw_chess.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
//    }

    void myGame(){
        for(int i = 0 ; i < BOARDSIZE ; i ++ ){
            for(int j = 0; j < BOARDSIZE ; j++){
                arr_board[i][j] = KONGNUM;
            }
        }
//        arr_board[3][4] = BLACKNUM;
//        arr_board[1][2] = WHITENUM;
        whiteTimer=(Chronometer)this.findViewById(R.id.whiteTimer);
        blackTimer=(Chronometer)this.findViewById(R.id.blackTimer);
        whiteStepTimer=(Chronometer)this.findViewById(R.id.whiteStepTimer);
        blackStepTimer=(Chronometer)this.findViewById(R.id.blackStepTimer);
        whiteStepTimer.setText(stepTime+"s");
        blackStepTimer.setText(stepTime+"s");
        //设置单步计时器的时间格式
        whiteStep=stepTime;
        blackStep=stepTime;
        whiteStepTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (whiteStep > 0)
                    whiteStep--;
                else {
                    //倒计时到了直接交换颜色
                    changeTimer(playColor);
                    playColor = playColor ^ 3;
                    whiteStep = stepTime;

                }
                chronometer.setText("" + whiteStep + "s");
            }
        });
        blackStepTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (blackStep > 0)
                    blackStep--;
                else {
                    changeTimer(playColor);
                    playColor = playColor ^ 3;
                    blackStep = stepTime;
                }
                chronometer.setText("" + blackStep + "s");
            }
        });
        blackTimer.start();
        blackStepTimer.start();
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

//                    extra_message.setText(temp_color);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int temp_color_int = Integer.parseInt(temp_color);
                int nowX = position / BOARDSIZE;
                int nowY = position % BOARDSIZE;
                //处理悔棋
                if(temp_color_int==KONGNUM)
                {
                    if(getSum==1)
                    {
                        if(playColor==WHITENUM)
                        {
                            arr_board[lastWhite.x[0]][lastWhite.y[0]]=KONGNUM;
                            lastWhite.len=0;
                        }
                        else
                        {
                            arr_board[lastBlack.x[0]][lastBlack.y[0]]=KONGNUM;
                            lastBlack.len=0;
                        }
                        getSum=0;
                        chessGridAdapter.notifyDataSetChanged();
                    }
                }
                else if(temp_color_int==LOSE)
                {
                    //处理对方认输
                    isLose=true;
                    drawGameRes(myColor);
                    isGameover=true;
                    //TODO:弹出win.jpg并可按返回按钮关闭dialog
                }
                else
                {
                    //处理收到的落子信息
                    getSum++;

                    //根据当前玩家颜色来落对应的子
                    if (playColor == BLACKNUM) {
                        arr_board[nowX][nowY] = BLACKLAST;
                        lastBlack.x[getSum - 1] = nowX;
                        lastBlack.y[getSum - 1] = nowY;
                        lastBlack.len = getSum;
                        if (getSum == 1) {
                            for (int i = 0; i < lastWhite.len; i++) {
                                arr_board[lastWhite.x[i]][lastWhite.y[i]] = WHITENUM;
                            }
                        }
                    } else if (playColor == WHITENUM) {
                        arr_board[nowX][nowY] = WHITELAST;
                        lastWhite.x[getSum - 1] = nowX;
                        lastWhite.y[getSum - 1] = nowY;
                        lastWhite.len = getSum;
                        if (getSum == 1) {
                            for (int i = 0; i < lastBlack.len; i++) {
                                arr_board[lastBlack.x[i]][lastBlack.y[i]] = BLACKNUM;
                            }
                        }
                    }
                    //arr_board[nowX][nowY] = temp_color_int;
                    chessGridAdapter.notifyDataSetChanged();
                    //更新该会话下面的已读状态
                    c.updateReceiveStatus(msg);

                    //变色
                    if (steps == 1 && playColor == BLACKNUM && getSum == 1) {

                        changeTimer(playColor);
                        playColor ^= 3;
                        getSum = 0;
                        steps++;
                        view_steps.setText("第" + steps + "手");
                    } else if (getSum == 2) {
                        getSum = 0;
                        changeTimer(playColor);
                        playColor ^= 3;
                        steps++;
                        view_steps.setText("第" + steps + "手");
                    }
                    //判断胜负
                    if (checkWin(nowX, nowY)) {
                        drawGameRes(myColor^3);
                        if (arr_board[nowX][nowY] == WHITENUM) {
                            Toast.makeText(NetFightActivity.this, "游戏结束！白方获胜！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NetFightActivity.this, "游戏结束！黑方获胜！", Toast.LENGTH_SHORT).show();
                        }
                    } else if (chechDraw()) {
                        //drawGameRes(KONGNUM);
                        Toast.makeText(NetFightActivity.this, "游戏结束！平局！", Toast.LENGTH_SHORT).show();
                    }
                }
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

    //检查未出界
    private boolean inBoard(int x,int y)
    {
        if(x>=0 && x<BOARDSIZE && y>=0 && y<BOARDSIZE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //交换计时
    private void changeTimer(int color)
    {
        if(color==WHITENUM)
        {
            whiteTimer.stop();
            whiteStepTimer.stop();
            whiteStepTimer.setText(stepTime+"s");
            String time[]= blackTimer.getText().toString().split(":");
            int temp=Integer.parseInt(time[0])*60;
            temp+=Integer.parseInt(time[1]);
            blackTimer.setBase(SystemClock.elapsedRealtime() -temp*1000);
            //交换计时并且重置倒计时器
            blackStep=stepTime;
            blackTimer.start();
            blackStepTimer.start();
        }
        else
        {
            blackTimer.stop();
            blackStepTimer.stop();
            blackStepTimer.setText(stepTime+"s");

            String time[]= whiteTimer.getText().toString().split(":");
            int temp=Integer.parseInt(time[0])*60;
            temp+=Integer.parseInt(time[1]);
            whiteTimer.setBase(SystemClock.elapsedRealtime() - temp * 1000);
            //交换计时并且重置倒计时器
            whiteStep=stepTime;
            whiteTimer.start();
            whiteStepTimer.start();
        }
    }

    private boolean checkWin(int posx,int posy)
    {
        int color=arr_board[posx][posy]%3;
        int connectSum;
        int nextx,nexty;
        for(int i=0;i<4;i++)
        {
            connectSum=1;
            nextx=posx+dir[i][0];
            nexty=posy+dir[i][1];
            while(inBoard(nextx,nexty) && arr_board[nextx][nexty]%3==color)
            {
                connectSum++;
                nextx+=dir[i][0];
                nexty+=dir[i][1];
            }
            nextx=posx-dir[i][0];
            nexty=posy-dir[i][1];
            while(inBoard(nextx,nexty) && arr_board[nextx][nexty]%3==color)
            {
                connectSum++;
                nextx-=dir[i][0];
                nexty-=dir[i][1];
            }
            if(connectSum>=6)
            {
                isGameover=true;
                return true;
            }
        }
        return false;
    }

    //检查是否平局
    private boolean chechDraw()
    {
        for(int i=0;i<BOARDSIZE;i++)
        {
            for(int j=0;j<BOARDSIZE;j++)
            {
                if(arr_board[i][j]==KONGNUM)
                {
                    return false;
                }
            }
        }
        isGameover=true;
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isGameover==false && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showDialog(1);
            return true;
        }else if(isGameover==false && keyCode == KeyEvent.KEYCODE_HOME)
        {
            showDialog(2);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            startActivity(CatalogLoggedActivity.class, null, true);
            return true;
        }
        else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            return new AlertDialog.Builder(NetFightActivity.this)
                    .setMessage("是否确定认输并返回?")
                    .setTitle("认输并返回")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    sentNetFightMessage(0, LOSE);
                                    startActivity(CatalogLoggedActivity.class,null,true);
                                    finish();

                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();

                                }
                            }).create();

        }
        else if(id==2)
        {
            return new AlertDialog.Builder(NetFightActivity.this)
                    .setMessage("是否确定认输并回到主页?")
                    .setTitle("认输并回到主页")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    sentNetFightMessage(0, LOSE);
                                    startActivity(CatalogLoggedActivity.class,null,true);
                                    finish();

                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();

                                }
                            }).create();

        }
        return null;

    }

    private void drawGameRes(int winColor)
    {
        if(isLose)
        {
            if(winColor==WHITENUM)
            {
                view_steps.setText("黑方认输！白方获胜！");
            }
            else
            {
                view_steps.setText("白方认输！黑方获胜！");
            }
        }
        else
        {
            if(winColor==BLACKNUM)
            {
                view_steps.setText("游戏结束！黑方获胜！");
            }
            else if(winColor==WHITENUM)
            {
                view_steps.setText("游戏结束！白方获胜！");
            }
            else if(winColor==KONGNUM)
            {
                view_steps.setText("游戏结束！平局！");
            }
        }
        isGameover=true;
        whiteStepTimer.stop();
        whiteTimer.stop();
        blackStepTimer.stop();
        blackTimer.stop();
    }
}