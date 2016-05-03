package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import adapter.ChessGridAdapter;
import core.Config;
import com.softwareprojectmanagement.liuziqi.jelly.move;

import org.w3c.dom.Text;

/**
 * Created by Lily on 16/3/14.
 * 实现一个简易的棋盘界面,点击落子,无算法无AI
 * 应该能适配不同屏幕
 * 目前棋盘为15*15,更改棋盘大小只需修改BOARDSIZE值
 * 人机GameView
 */
public class GameView2 extends AppCompatActivity {
    private int KONGNUM = Config.KONGNUM;
    private int BLACKNUM = Config.BLACKNUM;
    private int WHITENUM = Config.WHITENUM;
    private int BOARDSIZE = Config.BOARDSIZE;
    private int BLACKLAST=Config.BLACKLAST;
    private int WHITELAST=Config.WHITELAST;
    private int SELECTPOS=Config.SELECTPOS;
    private int screen_width;//屏幕宽度
    private int screen_height;//屏幕高度

    private int arr_board[][] = new int[BOARDSIZE][BOARDSIZE];

    private GridView gv_gameView;
    private ChessGridAdapter chessGridAdapter;

    private int chessSum=0;//下了第几个棋，每方走两个棋子
    private int dir[][]={{1,0},{1,1},{0,1},{1,-1}};
    private int playColor=BLACKNUM;
    private boolean isGameover=false;

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

    //显示本地胜负结果TextView
    private TextView view_blackRes;
    private TextView view_whiteRes;


    //是否有选择框存在
    private boolean select=false;
    private double selX,selY;   //当前鼠标位置
    private int posX,posY;      //当前选中位置
    private double itemSize;    //格子的宽高
    private double downX,downY; //上次鼠标位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.include_board);
        initView();
        myGame();
    }
    void initView() {
        gv_gameView = (GridView) findViewById(R.id.gridview);
        gv_gameView.setNumColumns(BOARDSIZE);

        WindowManager wm = this.getWindowManager();
        screen_width = wm.getDefaultDisplay().getWidth();
        screen_height=wm.getDefaultDisplay().getHeight();


        //因为gridview每个小格子的长宽必须是整数,所以设置重新设置一下棋盘的大小
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) gv_gameView.getLayoutParams();
        linearParams.height = screen_height*525/1000; //棋盘所占的权值
        itemSize=screen_height*525/1000 / BOARDSIZE;
        linearParams.width = linearParams.height;
        gv_gameView.setLayoutParams(linearParams);
        //为GridView设置适配器
        chessGridAdapter = new ChessGridAdapter(this, linearParams.width,arr_board);//lily
        gv_gameView.setAdapter(chessGridAdapter);

        view_steps=(TextView)this.findViewById(R.id.text_playturn);
        view_blackRes=(TextView)this.findViewById(R.id.blackRes);
        view_whiteRes=(TextView)this.findViewById(R.id.whiteRes);


        //绑定各个按钮事件，本地游戏隐藏聊按钮
        btn_return=(Button)this.findViewById(R.id.btn_return);
        //btn_chat=(Button)this.findViewById(R.id.btn_chat);
        btn_lose=(Button)this.findViewById(R.id.btn_lose);

        //btn_chat.setVisibility(View.GONE);

        //悔棋按钮事件
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isGameover && lastBlack.len>0 && lastWhite.len>0 && steps>2)
                {
                    if(chessSum==0) {
                        for (int i = 0; i < lastBlack.len; i++) {
                            arr_board[lastBlack.x[i]][lastBlack.y[i]] = KONGNUM;
                        }
                        for (int i = 0; i < lastWhite.len; i++) {
                            arr_board[lastWhite.x[i]][lastWhite.y[i]] = KONGNUM;
                        }
                        lastBlack.len = 0;
                        lastWhite.len = 0;
                        steps -= 2;
                        chessGridAdapter.notifyDataSetChanged();
                        view_steps.setText("第"+steps+"手");
                    }
                    else if(chessSum==1)
                    {
                        if(playColor==WHITENUM)
                        {
                            arr_board[lastWhite.x[0]][lastWhite.y[0]] = KONGNUM;
                            lastWhite.len = 0;
                        }
                        else
                        {
                            arr_board[lastBlack.x[0]][lastBlack.y[0]] = KONGNUM;
                            lastBlack.len = 0;
                        }
                        chessGridAdapter.notifyDataSetChanged();
                        chessSum=0;
                    }
                }
            }
        });

        //认输按钮事件
        btn_lose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGameover) {
                    isGameover=true;
                    drawGameRes(playColor ^ 3);
                }
            }
        });



        //绑定选择框移动事件
        gv_gameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        downX=event.getY();
                        downY=event.getX();
                        if(select==false)
                        {
                            selX=downX;
                            selY=downY;
                            select=true;
                            //显示选择框
                            posX=(int)(selX/itemSize);
                            posY=(int)(selY/itemSize);
                            if(inBoard(posX,posY))
                            {
                                arr_board[posX][posY]+=SELECTPOS;
                                chessGridAdapter.notifyDataSetChanged();
                            }
                        }
                        else
                        {
                            return false;
                        }
                    }
                    case MotionEvent.ACTION_MOVE:
                    {
                        selX+=(event.getY()-downX);
                        selY+=(event.getX()-downY);
                        downX=event.getY();
                        downY=event.getX();
                        int nowPosX=(int)(selX/itemSize);
                        int nowPosY=(int)(selY/itemSize);
                        if(posX==nowPosX && posY==nowPosY)
                        {
                            return false;
                        }
                        else if(inBoard(nowPosX,nowPosY))
                        {
                            arr_board[posX][posY]-=SELECTPOS;
                            arr_board[nowPosX][nowPosY]+=SELECTPOS;
                            posX=nowPosX;
                            posY=nowPosY;
                            chessGridAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            //边界处理，越界归位
                            selX=posX*itemSize;
                            selY=posY*itemSize;
                            downX=event.getY();
                            downY=event.getX();
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
                if (isGameover)
                    return;

                int nowX, nowY;
                nowX = posX;
                nowY = posY;
                if (arr_board[nowX][nowY]%9 == KONGNUM) {
                    chessSum++;

                    //根据当前玩家颜色来落对应的子
                    if (playColor == BLACKNUM) {
                        arr_board[nowX][nowY] = BLACKLAST;
                        lastBlack.x[chessSum-1]=nowX;
                        lastBlack.y[chessSum-1]=nowY;
                        lastBlack.len=chessSum;
                        if(chessSum==1)
                        {
                            for(int i=0;i<lastWhite.len;i++)
                            {
                                arr_board[lastWhite.x[i]][lastWhite.y[i]]=WHITENUM;
                            }
                        }
                    } else if (playColor == WHITENUM) {
                        arr_board[nowX][nowY] = WHITELAST;
                        lastWhite.x[chessSum-1]=nowX;
                        lastWhite.y[chessSum-1]=nowY;
                        lastWhite.len=chessSum;
                        if(chessSum==1)
                        {
                            for(int i=0;i<lastBlack.len;i++)
                            {
                                arr_board[lastBlack.x[i]][lastBlack.y[i]]=BLACKNUM;
                            }
                        }
                    }

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
                        view_steps.setText("第"+steps+"手");
                    }

                    chessGridAdapter.notifyDataSetChanged();//更新数据,刷新
                    //判断胜负
                    if (checkWin(nowX, nowY)) {
                        drawGameRes(arr_board[nowX][nowY]);
                        if (arr_board[nowX][nowY] == WHITENUM) {
                            Toast.makeText(GameView2.this, "游戏结束！白方获胜！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GameView2.this, "游戏结束！黑方获胜！", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(chechDraw())
                    {
                        drawGameRes(KONGNUM);
                    }

                    select=false;
                }

            }
        });
    }

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
                if(whiteStep>0)
                    whiteStep--;
                else
                {
                    //倒计时到了直接交换颜色
                    changeTimer(playColor);
                    playColor=playColor^3;
                    whiteStep=stepTime;

                }
                chronometer.setText( "" + whiteStep+"s");
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
                    blackStep=stepTime;
                }
                chronometer.setText("" + blackStep+"s");
            }
        });
        blackTimer.start();
        blackStepTimer.start();
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

    private void drawGameRes(int winColor)
    {
        if(winColor==BLACKNUM)
        {
            view_whiteRes.setText("负");
            view_whiteRes.setTextColor(Color.rgb(0, 0, 255));
            view_blackRes.setText("胜");
            view_blackRes.setTextColor(Color.rgb(255, 0, 0));
            view_steps.setText("游戏结束！黑方获胜！");
            whiteStepTimer.stop();
            whiteTimer.stop();
            isGameover=true;
        }
        else if(winColor==WHITENUM)
        {
            view_blackRes.setText("负");
            view_blackRes.setTextColor(Color.rgb(0,0,255));
            view_whiteRes.setText("胜");
            view_whiteRes.setTextColor(Color.rgb(255,0,0));
            view_steps.setText("游戏结束！白方获胜！");
            blackStepTimer.stop();
            blackTimer.stop();
            isGameover=true;
        }
        else if(winColor==KONGNUM)
        {
            view_blackRes.setText("平");
            view_blackRes.setTextColor(Color.rgb(0,0,255));
            view_whiteRes.setText("平");
            view_whiteRes.setTextColor(Color.rgb(0, 0, 255));
            whiteStepTimer.stop();
            whiteTimer.stop();
            blackStepTimer.stop();
            blackTimer.stop();
            isGameover=true;
        }
    }
}
