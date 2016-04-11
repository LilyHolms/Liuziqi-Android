package com.softwareprojectmanagement.liuziqi.lily.ui;

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
import android.widget.LinearLayout;
import android.widget.Toast;

import adapter.ChessGridAdapter;
import core.Config;

/**
 * Created by Lily on 16/3/14.
 * 实现一个简易的棋盘界面,点击落子,无算法无AI
 * 应该能适配不同屏幕
 * 目前棋盘为15*15,更改棋盘大小只需修改BOARDSIZE值
 */
public class GameView2 extends AppCompatActivity {
    private int KONGNUM = Config.KONGNUM;
    private int BLACKNUM = Config.BLACKNUM;
    private int WHITENUM = Config.WHITENUM;
    private int BOARDSIZE = Config.BOARDSIZE;
    private int screen_width;//屏幕宽度

    private int countClick=0;
    private int arr_board[][] = new int[BOARDSIZE][BOARDSIZE];

    private GridView gv_gameView;
    private ChessGridAdapter chessGridAdapter;
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

        //为GridView设置适配器
        chessGridAdapter = new ChessGridAdapter(this,screen_width,arr_board);//lily
        gv_gameView.setAdapter(chessGridAdapter);

        //因为gridview每个小格子的长宽必须是整数,所以设置重新设置一下棋盘的大小
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) gv_gameView.getLayoutParams();
        linearParams.height = (screen_width / BOARDSIZE) * BOARDSIZE;
        linearParams.width = linearParams.height;
        gv_gameView.setLayoutParams(linearParams);

        //注册监听事件
        gv_gameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//落子
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if(arr_board[position / BOARDSIZE][position % BOARDSIZE] == KONGNUM){
                    countClick++;
                    Toast.makeText(GameView2.this, "第"+countClick+"子,落子位置:" + position, Toast.LENGTH_SHORT).show();
                    if(countClick%2==0){
                        arr_board[position / BOARDSIZE][position % BOARDSIZE] = WHITENUM;
                    }else{
                        arr_board[position / BOARDSIZE][position % BOARDSIZE] = BLACKNUM;
                    }
                    chessGridAdapter.notifyDataSetChanged();//更新数据,刷新
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
        arr_board[3][4] = BLACKNUM;
        arr_board[1][2] = WHITENUM;
    }

}
