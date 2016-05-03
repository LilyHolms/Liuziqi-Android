package com.softwareprojectmanagement.liuziqi.lily.ui;

/**
 * Created by Lily on 16/4/6.
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.ChessGridNetAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.exception.BmobException;
import core.Config;
import entity.NetFightMessage;

public class NetFightFragment extends BaseFragment implements OnClickListener {

    private int KONGNUM = Config.KONGNUM;
    private int BLACKNUM = Config.BLACKNUM;
    private int WHITENUM = Config.WHITENUM;
    private int BOARDSIZE = Config.BOARDSIZE;
    private int screen_width;//屏幕宽度
    private int arr_board[][] = new int[BOARDSIZE][BOARDSIZE];



//    @Bind(R.id.extra_message)
//    TextView extra_message;
//    @Bind(R.id.opponent_name)
//    TextView opponent_name;

    @Bind(R.id.gridview)
    GridView gv_gameView;

    private ChessGridNetAdapter chessGridAdapter;

    BmobIMConversation c;

//    private TextView tv_text;
    protected View rootView = null;

    protected String title() {
        return c.getConversationTitle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.activity_net_fight, null);
        ButterKnife.bind(this, rootView);
        Bundle bundle = getArguments();
        c= BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) bundle.getSerializable("c"));

        myInit();
        myGame();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
//        tv_text=(TextView)getActivity().findViewById(R.id.tv_fragment1);
//        tv_text.setOnClickListener(this);

    }

    private void myInit(){

        gv_gameView.setNumColumns(BOARDSIZE);

        WindowManager wm = getActivity().getWindowManager();
        screen_width = wm.getDefaultDisplay().getWidth();

        //为GridView设置适配器
        chessGridAdapter = new ChessGridNetAdapter(getActivity(),screen_width,arr_board, c);//lily not sure "getContext()"
        gv_gameView.setAdapter(chessGridAdapter);

        //注册监听事件
        gv_gameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//落子
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if(arr_board[position / BOARDSIZE][position % BOARDSIZE] == KONGNUM){
                    toast("落子位置:" + position );
                    arr_board[position / BOARDSIZE][position % BOARDSIZE] = BLACKNUM;//这里暂时写成本方是黑子
                    chessGridAdapter.notifyDataSetChanged();//更新数据,刷新

                    sentNetFightMessage(position,WHITENUM);//这里暂时写成对方是白子
                }
            }
        });
    }
    private void myGame(){
        for(int i = 0 ; i < BOARDSIZE ; i ++ ){
            for(int j = 0; j < BOARDSIZE ; j++){
                arr_board[i][j] = KONGNUM;
            }
        }
        arr_board[3][4] = BLACKNUM;
        arr_board[1][2] = WHITENUM;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
//            case R.id.shiyishi:
//                sentNetFightMessage(12, BLACKNUM);
//                break;
            default:
                break;
        }

    }


    /**
//     * 发送自定义消息，比如：好友请求
//     */
//    //TODO:发送自定义消息，比如：好友请求
//    public void sendOtherMessage(){
//        AddFriendMessage msg =new AddFriendMessage();
//        msg.setContent("XXX添加你为好友");
//        Map<String,Object> map =new HashMap<>();
//        map.put("message", "很高兴认识你，可以加个好友吗？");
//        msg.setExtraMap(map);
//        c.sendMessage(msg, new MessageSendListener() {
//            @Override
//            public void done(BmobIMMessage msg, BmobException e) {
//                Logger.i("othermsg:" + msg.toString());
//                if (e == null) {//发送成功
//                    toast("发送成功");
//                } else {//发送失败
//                    toast("发送失败:" + e.getMessage());
//                }
//            }
//        });
//    }

    /**
     * 发送下棋消息
     * @param coordinate 棋盘上坐标
     * @param chesscolor 棋子颜色黑棋或白棋
     */
    public void sentNetFightMessage(int coordinate, int chesscolor){
        NetFightMessage msg = new NetFightMessage();
        msg.setContent("对战,坐标:"+coordinate);
        Map<String,Object> map = new HashMap<>();
        map.put("COORDINATE",coordinate);
        map.put("COLOR", chesscolor);//TODO:同意对战时,就确定黑白子颜色,此处不用传参
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

//    /**接收到聊天消息
//     * @param event
//     */
//    @Subscribe
//    public void onEventMainThread(MessageEvent event){
//        BmobIMMessage msg =event.getMessage();
//        addMessage2Chat(event);
//    }
//
//    /**添加消息到下棋界面中
//     * @param event
//     */
//    private void addMessage2Chat(MessageEvent event){
//        BmobIMMessage msg =event.getMessage();
//        Logger.i("接收到消息：" + msg.getContent());
//        System.out.println("shiyishi" + c.getConversationId());
//        System.out.println(event.getConversation().getConversationId());
//
//        if(c!=null && event!=null && c.getConversationId().equals(event.getConversation().getConversationId()) //如果是当前会话的消息
//        // lily not sure这里把getConversation改成了getMessage()
//                ){//取消 "并且不为暂态消息"
//            if(chessGridAdapter.findPosition(msg)<0){//如果未添加到界面中
//                chessGridAdapter.addMessage(msg);
//
//                //shiyishi
////                final NetFightMessage message = (NetFightMessage) o;
//                String content = msg.getContent();
//
//                zuobiao.setText(content);//这里消息类型不知道怎么调整
//                final BmobIMUserInfo info = msg.getBmobIMUserInfo();
////        ViewUtil.setAvatar(info != null ? info.getAvatar() : null, R.mipmap.head, iv_avatar);
//                opponent_name.setText(info.getName());//显示对手name
//                int position = Integer.getInteger(content);
//                Logger.i("试一试打印坐标：" + position);
//                arr_board[position / BOARDSIZE][position % BOARDSIZE] = WHITENUM;//这里暂时写成本方是黑子
//                chessGridAdapter.notifyDataSetChanged();//更新数据,刷新
//                //shiyishi
//                //更新该会话下面的已读状态
//                c.updateReceiveStatus(msg);
//            }
//        }else{
//            Logger.i("不是与当前聊天对象的消息");
//        }
//    }

    //shiyishi

    public void bindData(Object o) {//不知道有没有用
        final NetFightMessage message = (NetFightMessage) o;
        String content = message.getContent();
        //extra_message.setText(content);//这里消息类型不知道怎么调整
        final BmobIMUserInfo info = message.getBmobIMUserInfo();
//        ViewUtil.setAvatar(info != null ? info.getAvatar() : null, R.mipmap.head, iv_avatar);
        //opponent_name.setText(info.getName());//显示对手name

    }
    //shiyishi end

    /**
     * 添加未读的通知栏消息到聊天界面
     */
    private void addUnReadMessage(){
        List<MessageEvent> cache = BmobNotificationManager.getInstance(getActivity()).getNotificationCacheList();//lily
        if(cache.size()>0){
            int size =cache.size();
            for(int i=0;i<size;i++){
                MessageEvent event = cache.get(i);
//                addMessage2Chat(event);
            }
        }
    }

}
