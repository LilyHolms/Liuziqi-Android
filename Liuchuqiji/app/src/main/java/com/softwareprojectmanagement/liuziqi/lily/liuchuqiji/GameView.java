package com.softwareprojectmanagement.liuziqi.lily.liuchuqiji;

import android.content.Context;
import android.os.Bundle;
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
    private int firststep=0;//是否是第一步
    private int chessSum=0;//下了第几个棋，每方走两个棋子
    private int dir[][]={{1,0},{1,1},{0,1},{1,-1}};
    private int countClick=0;
    int arr_board[][] = new int[BOARDSIZE][BOARDSIZE];

    private GridView gv_gameView;
    private MyAdapter myAdapter;
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

        //为GridView设置适配器
        myAdapter = new MyAdapter(this);
        gv_gameView.setAdapter(myAdapter);
        //注册监听事件
        gv_gameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//落子
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int nowX,nowY;
                nowX=position/BOARDSIZE;
                nowY=position%BOARDSIZE;
                if(arr_board[nowX][nowY] == KONGNUM){
                    countClick++;
                    chessSum++;
                    //Toast.makeText(GameView.this, "第"+countClick+"子,落子位置:" + position, Toast.LENGTH_SHORT).show();
//                    if(countClick%2==0){
//                        arr_board[position / BOARDSIZE][position % BOARDSIZE] = WHITENUM;
//                    }else{
//                        arr_board[position / BOARDSIZE][position % BOARDSIZE] = BLACKNUM;
//                    }
                    //根据当前玩家颜色来落对应的子
                    if(playColor==BLACKNUM)
                    {
                        arr_board[nowX][nowY] = BLACKNUM;
                    }
                    else if(playColor==WHITENUM)
                    {
                        arr_board[nowX][nowY] = WHITENUM;
                    }
                    //判断胜负
                    if(checkWin(playColor,nowX,nowY))
                    {
                        if(playColor==WHITENUM)
                        {
                            Toast.makeText(GameView.this, "游戏结束！白方获胜！", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(GameView.this, "游戏结束！黑方获胜！", Toast.LENGTH_SHORT).show();
                        }
                    }
                    //变色
                    if(firststep==0 && playColor==BLACKNUM && chessSum==1)
                    {
                        firststep=1;
                        playColor^=3;
                        chessSum=0;
                    }
                    else if(chessSum==2)
                    {
                        chessSum=0;
                        playColor^=3;
                    }

                    myAdapter.notifyDataSetChanged();//更新数据,刷新
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
//        arr_board[3][4] = BLACKNUM;
//        arr_board[1][2] = WHITENUM;
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
            nexty=posy-dir[i][0];
            while(inBoard(nextx,nexty) && arr_board[nextx][nexty]==color)
            {
                connectSum++;
                nextx-=dir[i][0];
                nexty-=dir[i][1];
            }
            if(connectSum>=6)
            {
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
}
