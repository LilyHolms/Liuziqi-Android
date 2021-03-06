package adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.softwareprojectmanagement.liuziqi.lily.ui.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.newim.bean.BmobIMMessage;
import core.Config;
/**
 * Created by Lily on 16/4/8.
 */
//自定义适配器
public class ChessGridAdapter extends BaseAdapter {
    //上下文对象
    private Context context;
    private int KONGNUM = Config.KONGNUM;
    private int BLACKNUM = Config.BLACKNUM;
    private int WHITENUM = Config.WHITENUM;
    private int BOARDSIZE = Config.BOARDSIZE;
    private int screen_width;
    private int arr_board[][];

    private int BLACKLAST=Config.BLACKLAST;
    private int WHITELAST=Config.WHITELAST;
    private int SELECTPOS=Config.SELECTPOS;
    public ChessGridAdapter() {
        //子类添加构造函数ChessGridNetAdapter(Context context,BmobIMConversation c)时,要求给父类添加无参构造函数
    }

    public ChessGridAdapter(Context context) {
        this.context = context;
    }

    public ChessGridAdapter(Context context, int screen_width, int arr_board[][]) {
        this.context = context;
        this.screen_width = screen_width;
        this.arr_board = arr_board;//lily
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

        int type=arr_board[position/BOARDSIZE][position%BOARDSIZE]%9;
        int issel=arr_board[position/BOARDSIZE][position%BOARDSIZE]/9;
        if(type==BLACKNUM){
            imageView.setImageResource(R.drawable.blackchess);
        }else if(type==WHITENUM){
            imageView.setImageResource(R.drawable.whitechess);
        }else if(type==KONGNUM){
            imageView.setImageResource(R.color.transparent);//shiyishi
        }else if(type==WHITELAST){
            imageView.setImageResource(R.drawable.whitechess_sel);
            //arr_board[position/BOARDSIZE][position%BOARDSIZE]=WHITENUM;
        }else if(type==BLACKLAST){
            imageView.setImageResource(R.drawable.blackchess_sel);
            //arr_board[position/BOARDSIZE][position%BOARDSIZE]=BLACKNUM;
        }
        if(issel==1)
        {
            imageView.setBackgroundResource(R.drawable.select_frame);
        }
        else
        {
            imageView.setBackgroundResource(0);
        }
        return imageView;
    }
}