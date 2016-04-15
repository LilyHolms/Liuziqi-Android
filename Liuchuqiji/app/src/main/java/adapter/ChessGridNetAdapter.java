package adapter;

import android.content.Context;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.v3.BmobUser;

/**
 * Created by Lily on 16/4/8.
 */
public class ChessGridNetAdapter extends ChessGridAdapter{

    //信息类型
    private final int TYPE_RECEIVER_CHESS = 8;
    private final int TYPE_SEND_CHESS = 9;

    private List<BmobIMMessage> msgs = new ArrayList<>();
    private String currentUid="";
    BmobIMConversation c;

    public ChessGridNetAdapter(Context context) {
        super(context);
    }
    public ChessGridNetAdapter(Context context,BmobIMConversation c) {
        try {
            currentUid = BmobUser.getCurrentUser(context).getObjectId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.c =c;
    }

    public ChessGridNetAdapter(Context context, int screen_width, int arr_board[][]){
        super(context,screen_width,arr_board);
    }

    public ChessGridNetAdapter(Context context, int screen_width, int arr_board[][],BmobIMConversation c){
        super(context,screen_width,arr_board);
        try {
            currentUid = BmobUser.getCurrentUser(context).getObjectId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.c =c;
    }

    public int findPosition(BmobIMMessage message) {
        int index = this.getCount();
        int position = -1;
        while(index-- > 0) {
            if(message.equals(this.getItem(index))) {
                position = index;
                break;
            }
        }
        return position;
    }
//    public void addMessages(List<BmobIMMessage> messages) {
//        msgs.addAll(0, messages);
//        notifyDataSetChanged();
//    }
//    public void addMessage(BmobIMMessage message) {
//        msgs.addAll(Arrays.asList(message));
//        notifyDataSetChanged();
//    }

    /**获取消息
     * @param position
     * @return
     */
    public BmobIMMessage getItem(int position){
        return this.msgs == null?null:(position >= this.msgs.size()?null:this.msgs.get(position));
    }

    /**移除消息
     * @param position
     */
    public void remove(int position){
        msgs.remove(position);
        notifyDataSetChanged();
    }

//    public BmobIMMessage getFirstMessage() {
//        if (null != msgs && msgs.size() > 0) {
//            return msgs.get(0);
//        } else {
//            return null;
//        }
//    }

}
