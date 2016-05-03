package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.ChatAdapter;
import adapter.OnRecyclerViewListener;
import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.BmobRecordManager;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.newim.listener.ObseverListener;
import cn.bmob.newim.listener.OnRecordChangeListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import core.Config;
import entity.AddFriendMessage;
import entity.Friends;
import entity.InViteToFightAgreeMessage;
import entity.InviteToFightMessage;
import model.UserModel;

public class NetChatActivity extends BaseActivity implements ObseverListener{

    @Bind(R.id.ll_chat)
    LinearLayout ll_chat;

    @Bind(R.id.sw_refresh)
    SwipeRefreshLayout sw_refresh;

    @Bind(R.id.rc_view)
    RecyclerView rc_view;

    @Bind(R.id.edit_msg)
    EditText edit_msg;

    @Bind(R.id.btn_chat_send)
    Button btn_chat_send;

    @Bind(R.id.btn_invitetoFight)
    Button btn_invitetoFight;

    @Bind(R.id.btn_AddFriend)
    Button btn_AddFriend;

    @Bind(R.id.tv_title)
    TextView tv_title;
    boolean isWaiting = false;//标记是否已发送对战邀请,并正在等待回复,0没有等待,1正在等待
    // TODO:isWaiting还有一些地方要修改:对战过程中结束后
    static BmobIMMessage receiveInviteMsg = null;//收到的邀请消息,为了跳转Activity此值不变,设置成了静态

    Toast toast;

    ChatAdapter adapter;
    protected LinearLayoutManager layoutManager;
    BmobIMConversation c;

    protected String title() {
        return c.getConversationTitle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_chat);

        c= BmobIMConversation.obtain(BmobIMClient.getInstance(), (BmobIMConversation) getBundle().getSerializable("c"));
        initSwipeLayout();
        initBottomView();
        isFriend();
        tv_title.setText(c.getConversationTitle());
    }


    //判断是否是好友
    private void isFriend(){
        String friend_A = UserModel.getInstance().getCurrentUser().getUsername();
        String friend_B = c.getConversationTitle();
        BmobQuery<Friends> query = new BmobQuery<Friends>();
        //查询playerName叫“比目”的数据

        if( friend_A.compareTo(friend_B)<0 ){
            query.addWhereEqualTo("id", friend_A);
            query.addWhereEqualTo("friendId", friend_B);
        }else{
            query.addWhereEqualTo("id", friend_B);
            query.addWhereEqualTo("friendId", friend_A);

        }
        query.findObjects(this, new FindListener<Friends>() {
            @Override
            public void onSuccess(List<Friends> object) {
                if (object.size() != 0) {//已是好友
                    btn_AddFriend.setBackgroundResource(R.drawable.ic_chat_btn_are_friends);
                    btn_AddFriend.setEnabled(false);
                }

            }

            @Override
            public void onError(int code, String msg) {
            }
        });
    }

    //发送对战邀请
    @OnClick(R.id.btn_invitetoFight)
    public void onInvitetoFightClick(View view){
        if(!isWaiting){
            sendInvite2FightMessage();
        }else {
            toast("您已发送对战邀请!");
        }
    }

    @OnClick(R.id.btn_AddFriend)
    public void onAddFriendClick(View view){
        if(true){
            sendAddFriendMessage();
        }else{
            toast("对方已经是您的好友!");
        }
    }

    private void initSwipeLayout(){
        sw_refresh.setEnabled(true);
        layoutManager = new LinearLayoutManager(this);//RecyclerView需要显示的是横向滚动的列表或者竖直滚动的列表时使用
        rc_view.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(this,c);
        rc_view.setAdapter(adapter);
        ll_chat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ll_chat.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                sw_refresh.setRefreshing(true);
                //自动刷新
                queryMessages(null);
            }
        });
        //下拉加载
        sw_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BmobIMMessage msg = adapter.getFirstMessage();
                queryMessages(msg);
            }
        });
        //设置RecyclerView的点击事件
        adapter.setOnRecyclerViewListener(new OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                Logger.i("" + position);
            }

            @Override
            public boolean onItemLongClick(int position) {
                //长按就删除了本地消息
                c.deleteMessage(adapter.getItem(position));
                adapter.remove(position);
                return true;
            }
        });
    }

    private void initBottomView(){
        edit_msg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
                    scrollToBottom();
                }
                return false;
            }
        });
        edit_msg.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                scrollToBottom();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    btn_chat_send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @OnClick(R.id.btn_chat_send)
    public void onSendClick(View view){
        sendMessage();
    }


    /**
     * 根据是否点击笑脸来显示文本输入框的状态
     * @param  isEmo 用于区分文字和表情
     * @return void
     */
    private void showEditState(boolean isEmo) {
        edit_msg.requestFocus();
        if (isEmo) {
            hideSoftInputView();
        } else {
            showSoftInputView();
        }
    }

    /**
     * 显示软键盘
     */
    public void showSoftInputView() {
        if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .showSoftInput(edit_msg, 0);
        }
    }

    /**
     * 发送文本消息
     */
    private void sendMessage(){
        String text=edit_msg.getText().toString();
        if(TextUtils.isEmpty(text.trim())){
            toast("请输入内容");
            return;
        }
        BmobIMTextMessage msg =new BmobIMTextMessage();
        msg.setContent(text);
        //可设置额外信息
        Map<String,Object> map =new HashMap<>();
        map.put("level", "1");//随意增加信息
        msg.setExtraMap(map);
        c.sendMessage(msg, listener);
    }

    /**
     * 消息发送监听器
     */
    public MessageSendListener listener =new MessageSendListener() {

        @Override
        public void onProgress(int value) {
            super.onProgress(value);
            //文件类型的消息才有进度值
            Logger.i("onProgress："+value);
        }

        @Override
        public void onStart(BmobIMMessage msg) {
            super.onStart(msg);
            adapter.addMessage(msg);
            edit_msg.setText("");
            scrollToBottom();
        }

        @Override
        public void done(BmobIMMessage msg, BmobException e) {
            adapter.notifyDataSetChanged();
            edit_msg.setText("");
            scrollToBottom();
            if (e != null) {
                toast(e.getMessage());
            }
        }
    };

    /**
     * 发送对战消息
     */
    public void sendInvite2FightMessage(){
        InviteToFightMessage msg = new InviteToFightMessage();
        msg.setContent("一封战书");
        c.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                Logger.i("othermsg:" + msg.toString());
                if (e == null) {//发送成功
//                    btn_invitetoFight.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    btn_invitetoFight.setBackgroundResource(R.drawable.ic_chat_btn_fight_red);
                    isWaiting=true;//1表示正在等待
                    adapter.addMessage(msg);//将消息显示到本地列表
                    scrollToBottom();
                    toast("发送成功");
                } else {//发送失败
                    toast("发送失败:" + e.getMessage());
                }
            }
        });
    }

    /**
     * 发送添加好友申请
     */
    private void sendAddFriendMessage(){
        AddFriendMessage msg = new AddFriendMessage();
        msg.setContent("好友申请");
        c.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                Logger.i("othermsg:" + msg.toString());
                if (e == null) {//发送成功
                    adapter.addMessage(msg);//将消息显示到本地列表
                    scrollToBottom();
                    toast("发送成功");
                } else {//发送失败
                    toast("发送失败:" + e.getMessage());
                }
            }
        });
    }

    /**首次加载，可设置msg为null，下拉刷新的时候，默认取消息表的第一个msg作为刷新的起始时间点，默认按照消息时间的降序排列
     * @param msg
     */
    public void queryMessages(BmobIMMessage msg){
        c.queryMessages(msg, 10, new MessagesQueryListener() {
            @Override
            public void done(List<BmobIMMessage> list, BmobException e) {
                sw_refresh.setRefreshing(false);
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        adapter.addMessages(list);
                        layoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                    }
                } else {
                    toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }

    private void scrollToBottom() {
        layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
    }

    /**接收到聊天消息
     * @param event
     */
    @Subscribe
    public void onEventMainThread(MessageEvent event){
        addMessage2Chat(event);
    }

    /**添加消息到聊天界面中
     * @param event
     */
    private void addMessage2Chat(MessageEvent event){
        BmobIMMessage msg =event.getMessage();
        Logger.i("接收到消息：" + msg.getContent());
        if(c!=null && event!=null && c.getConversationId().equals(event.getConversation().getConversationId()) //如果是当前会话的消息
                && !msg.isTransient()){//并且不为暂态消息
            if(adapter.findPosition(msg)<0){//如果未添加到界面中
                adapter.addMessage(msg);
                //更新该会话下面的已读状态
                c.updateReceiveStatus(msg);
                if(msg.getContent().equals("一封战书")){
                    receiveInviteMsg = msg;
                }
            }
            scrollToBottom();
        }else if(c!=null && event!=null && c.getConversationId().equals(event.getConversation().getConversationId()) //如果是当前会话的消息
                && msg.isTransient()){//如果是暂态消息
            String content = msg.getContent();

            if( content.equals("agreeFight") ){//对方同意对战
                Bundle bundle = new Bundle();
                bundle.putSerializable("c", c);
                //startActivity(NetFightActivity.class, bundle, false);
                Intent intent=new Intent(this,NetFightActivity.class);
                intent.putExtra("myColor", Config.BLACKNUM);
                intent.putExtra("waitTime",30);
                //把bundle也放在Intent中传过去
                intent.putExtra("bundle",bundle);
                startActivity(intent);
            }else if(content.equals("rejectFight") ){//对方拒绝对战
//                btn_invitetoFight.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                btn_invitetoFight.setBackgroundResource(R.drawable.ic_chat_btn_fight);
                isWaiting=false;
                toast("对方拒绝邀请!");
            }else if(content.equals("withdrawFight")){//对方撤回邀请
                c.deleteMessage(receiveInviteMsg);
                adapter.remove(adapter.findPosition(receiveInviteMsg));
                adapter.notifyDataSetChanged();
            }else if(content.equals("agreeFriend")){
                //ps已在发送方点击同意按钮时将好友信息插入数据库
                toast("对方同意了您的好友申请");
                //已是好友
                btn_AddFriend.setBackgroundResource(R.drawable.ic_chat_btn_are_friends);
                btn_AddFriend.setEnabled(false);
            }else if(content.equals("rejectFriend")){
                toast("对方拒绝了您的好友申请");
            }
        }else{
            Logger.i("不是与当前聊天对象的消息");
        }
    }

    //当点击返回键时,先判断是否处于发送对战邀请等待对方回复的状态,如果是,不允许该离开界面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && isWaiting){
            toast("请先撤回对战邀请再离开页面!");
            return false;
        }else{
            startActivity(CatalogLoggedActivity.class, null, true);
            return super.onKeyDown(keyCode, event);
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
            int size =cache.size();
            for(int i=0;i<size;i++){
                MessageEvent event = cache.get(i);
                addMessage2Chat(event);
            }
        }
        scrollToBottom();
    }

    @Override
    protected void onPause() {
        //取消通知栏监听
        BmobNotificationManager.getInstance(this).removeObserver(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //更新此会话的所有消息为已读状态
        hideSoftInputView();
        c.updateLocalCache();
        super.onDestroy();
    }

}
