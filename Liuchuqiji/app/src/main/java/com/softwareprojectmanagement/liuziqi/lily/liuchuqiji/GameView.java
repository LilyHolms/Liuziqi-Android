package com.softwareprojectmanagement.liuziqi.lily.liuchuqiji;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * Created by Lily on 16/3/14.
 * 实现一个简易的棋盘界面,点击落子,无算法无AI
 * 应该能适配不同屏幕
 * 目前棋盘为15*15,更改棋盘大小只需修改BOARDSIZE值
 */
public class GameView extends AppCompatActivity {
    private int KONGNUM = 0;//没有棋子
    private int BLACKNUM = 1;//黑棋子
    private int WHITENUM = 2;//白棋子
    private int BOARDSIZE = 19;//棋盘大小
    private int screen_width;//屏幕宽度
    private int playColor=1;//当前走棋玩家颜色
    private int peopleColor=0;
    private int AIcolor;
    private int firststep=0;//是否是第一步
    private int chessSum=0;//下了第几个棋，每方走两个棋子
    private int dir[][]={{1,0},{1,1},{0,1},{1,-1}};
    private int countClick=0;
    private boolean isGameover=false;
    int arr_board[][] = new int[BOARDSIZE][BOARDSIZE];  //棋盘

    private GridView gv_gameView;
    private MyAdapter myAdapter;
    private Connect6AI myAI=new Connect6AI();   //AI
    private move nowMove=new move();
    private int AIaction=0; //是否是AI行动

    protected Handler updateBoardHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 1:
                {
                    updateDataSet();
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.include_board);
        myGame();   //初始化棋盘
        initView();
    }

    void initView() {
        gv_gameView = (GridView) findViewById(R.id.gridview);
        gv_gameView.setNumColumns(BOARDSIZE);

        //为GridView设置适配器
        myAdapter = new MyAdapter(this);
        gv_gameView.setAdapter(myAdapter);

        //设置玩家和AI颜色
        peopleColor=WHITENUM;
        initAI(BLACKNUM);

        //注册监听事件
        gv_gameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//落子
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //AI思考中，还未到玩家走棋
                if (playColor != peopleColor || isGameover)
                    return;
                int nowX, nowY;
                nowX = position / BOARDSIZE;
                nowY = position % BOARDSIZE;
                if (arr_board[nowX][nowY] == KONGNUM) {
                    countClick++;
                    chessSum++;
                    //根据当前玩家颜色来落对应的子
                    if (playColor == BLACKNUM) {
                        arr_board[nowX][nowY] = BLACKNUM;
                        nowMove.x[chessSum - 1] = nowX;
                        nowMove.y[chessSum - 1] = nowY;
                    } else if (playColor == WHITENUM) {
                        arr_board[nowX][nowY] = WHITENUM;
                        nowMove.x[chessSum - 1] = nowX;
                        nowMove.y[chessSum - 1] = nowY;
                    }
                    //判断胜负
                    if (checkWin(playColor, nowX, nowY)) {
                        if (playColor == WHITENUM) {
                            Toast.makeText(GameView.this, "游戏结束！白方获胜！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GameView.this, "游戏结束！黑方获胜！", Toast.LENGTH_SHORT).show();
                        }
                    }
                    //变色
                    if (firststep == 0 && playColor == BLACKNUM && chessSum == 1) {
                        nowMove.len = 1;
                        myAI.makeMove(nowMove, playColor);
                        firststep = 1;
                        playColor ^= 3;
                        chessSum = 0;
                        AIaction = 1;
                    } else if (chessSum == 2) {
                        nowMove.len = 2;
                        myAI.makeMove(nowMove, playColor);
                        chessSum = 0;
                        playColor ^= 3;
                        AIaction = 1;
                    }

                    myAdapter.notifyDataSetChanged();//更新数据,刷新

                    //AI思考下棋
                    if (AIaction == 1) {
                        new Thread() {
                            public void run() {
                                nowMove = myAI.getNextMove();
                                makeAImove(nowMove, playColor);
                                Message msg=new Message();
                                msg.what=1;
                                updateBoardHandler.sendMessage(msg);
                            }
                        }.start();
                    }
                }
            }
        });

        WindowManager wm = this.getWindowManager();
        screen_width = wm.getDefaultDisplay().getWidth();
    }

    void myGame(){
        for(int i = 0 ; i < BOARDSIZE ; i ++ ){
            for(int j = 0; j < BOARDSIZE ; j++){
                arr_board[i][j] = KONGNUM;
            }
        }
    }

    void updateDataSet()
    {
        myAdapter.notifyDataSetChanged();
        if (checkWin(playColor^3, nowMove.x[0], nowMove.y[0]) || checkWin(playColor^3, nowMove.x[1], nowMove.y[1])) {
            if (playColor == BLACKNUM) {
                Toast.makeText(GameView.this, "游戏结束！白方获胜！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GameView.this, "游戏结束！黑方获胜！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //自定义适配器
    class MyAdapter extends BaseAdapter {
        //上下文对象
        private Context context;

        MyAdapter(Context context) {
            this.context = context;
        }

        public int getCount() {
            return BOARDSIZE*BOARDSIZE;
        }

        public Object getItem(int item) {
            return item;
        }

        public long getItemId(int id) {
            return id;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams( screen_width / BOARDSIZE, screen_width / BOARDSIZE));
                //设置ImageView对象布局,即设置gridview每个item的宽高
                imageView.setAdjustViewBounds(true);//设置边界对齐
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);//设置刻度的类型
            } else {
                imageView = (ImageView) convertView;
            }

            if(arr_board[position/BOARDSIZE][position%BOARDSIZE]==BLACKNUM){
                imageView.setImageResource(R.drawable.blackchess);
            }else if(arr_board[position/BOARDSIZE][position%BOARDSIZE]==WHITENUM){
                imageView.setImageResource(R.drawable.whitechess);
            }

            return imageView;
        }
    }

    //判断游戏是否结束，当前颜色玩家是否已经获胜
    private boolean checkWin(int color,int posx,int posy)
    {
        int connectSum;
        int nextx,nexty;
        for(int i=0;i<4;i++)
        {
            connectSum=1;
            nextx=posx+dir[i][0];
            nexty=posy+dir[i][1];
            while(inBoard(nextx,nexty) && arr_board[nextx][nexty]==color)
            {
                connectSum++;
                nextx+=dir[i][0];
                nexty+=dir[i][1];
            }
            nextx=posx-dir[i][0];
            nexty=posy-dir[i][1];
            while(inBoard(nextx,nexty) && arr_board[nextx][nexty]==color)
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

    //初始化AI
    private void initAI(int color)
    {
        //初始化AI设置
        AIcolor=color;
        myAI.setMyColor(color);
        if(color==BLACKNUM)
        {
            nowMove=myAI.getNextMove();
            makeAImove(nowMove, color);
            myAdapter.notifyDataSetChanged();
        }
    }

    //执行AI招法
    private void makeAImove(move AImove,int color)
    {
        for(int i=0;i<AImove.len;i++)
        {
            arr_board[AImove.x[i]][AImove.y[i]]=color;
        }
        playColor^=3;
        AIaction=0;
    }
}
