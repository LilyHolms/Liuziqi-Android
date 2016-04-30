package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import adapter.BaseViewHolder;
import adapter.ChatAdapter;
import adapter.ChatRoomAdapter;
import adapter.OnRecyclerViewListener;
import adapter.ReceiveInviteToFightHolder;
import adapter.ReceiveTextHolder;
import adapter.ReiceiveAddFriendHolder;
import adapter.SendAddFriendHolder;
import adapter.SendInviteToFightHolder;
import adapter.SendTextHolder;
import butterknife.OnClick;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.newim.listener.ObseverListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.ValueEventListener;
import entity.ChatMessage;
import entity.User;
import entity.WhiteChessQueue;
import model.UserModel;

public class ChatRoomActivity extends BaseActivity implements ChatRoomAdapter.OnRecyclerViewListener{
    RecyclerView lv_data;
    Button btn_send;
    EditText et_name, et_content;

    ChatRoomAdapter myAdapter;
    List<ChatMessage> messages = new ArrayList<ChatMessage>();
    BmobRealTimeData data = new BmobRealTimeData();
    LinearLayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);


       // et_name = (EditText) findViewById(R.id.et_name);
        et_content = (EditText) findViewById(R.id.et_content);
        lv_data = (RecyclerView) findViewById(R.id.lv_data);

        lv_data.setHasFixedSize(true);//使RecyclerView保持固定的大小,这样会提高RecyclerView的性能。
        layoutManager= new LinearLayoutManager(this);

        lv_data.setLayoutManager(layoutManager);

        //initData();
        myAdapter = new ChatRoomAdapter(messages);
        myAdapter.setOnRecyclerViewListener(this);
        lv_data.setAdapter(myAdapter);
        UpdateHistory();

    }
    private void scrollToBottom() {
        layoutManager.scrollToPositionWithOffset(myAdapter.getItemCount() - 1, 0);
    }
    private void  UpdateHistory(){
        BmobQuery<ChatMessage> query = new BmobQuery<ChatMessage>();
        //query.setLimit(10);
        query.order("createdAt");/// 根据createdAt字段升序显示数据
        query.findObjects(this, new FindListener<ChatMessage>() {
            @Override
            public void onSuccess(List<ChatMessage> object) {
                // TODO Auto-generated method stub
                Logger.i("查询成功：共" + object.size() + "条数据。");
                //按时间排序
                for (ChatMessage mess : object) {
                    messages.add(mess);//收到信息
                    myAdapter.notifyDataSetChanged();
                    scrollToBottom();
                }
                //
                init();
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                //Toast.makeText(this, "查询失败：" + msg, Toast.LENGTH_SHORT).show();
                Logger.i("查询失败：" + msg);
            }
        });

    }
    private void init(){

        Toast.makeText(this, "聊天大厅", Toast.LENGTH_SHORT).show();
        data.start(this, new ValueEventListener() {//连接服务器
            public void onDataChange(JSONObject arg0) {//数据改变回调函数
                //在这里判断一下是不是自己发送的消息，选择对应的ui

                if (BmobRealTimeData.ACTION_UPDATETABLE.equals(arg0.optString("action"))) {
                    JSONObject data = arg0.optJSONObject("data");
                    ChatMessage msg = new ChatMessage(data.optString("UserObjectId"), data.optString("name"), data.optString("content"));
                    msg.setcreatetime(data.optString("createdAt"));
                    messages.add(msg);//收到信息
                    myAdapter.notifyDataSetChanged();
                    scrollToBottom();
                    Logger.i(data.optString("UserObjectId"));
                    Logger.i("数据改变 success" + myAdapter.getItemCount());

                }
            }

            public void onConnectCompleted() {
                if (data.isConnected()) {
                    data.subTableUpdate("ChatMessage");//监听数据改变
                }
            }
        });
    }
    @OnClick(R.id.btn_send)
    public void onToSendClick(View view){
        String content = et_content.getText().toString();
        if( TextUtils.isEmpty(content)){
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }else{
            sendMsg( content);
        }
    }

    /**
     * 根据是否点击笑脸来显示文本输入框的状态
     * @param  isEmo 用于区分文字和表情
     * @return void
     */
    private void showEditState(boolean isEmo) {
        et_content.requestFocus();
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
                        .showSoftInput(et_content, 0);
        }
    }
    @Override
    public void onItemClick(int position) {

        //showToastMessage("clicked: " + position);
    }
    @Override
    public boolean onItemLongClick(int position) {
        //showToastMessage("long clicked: " + position);
        myAdapter.notifyItemRemoved(position);
        messages.remove(position);
        myAdapter.notifyItemRangeChanged(position, myAdapter.getItemCount());
        return true;
    }

    private void sendMsg(String msg){
        Toast.makeText(this, "发送", Toast.LENGTH_SHORT).show();
        User user = UserModel.getInstance().getCurrentUser();//获取当前用户
        String name = user.getUsername();
        String UserObjectId = user.getObjectId();
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time=format.format(date);
        final ChatMessage chat = new ChatMessage(UserObjectId,name, msg);
        chat.setcreatetime(time);
        chat.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                et_content.setText("");

            }

            @Override
            public void onFailure(int arg0, String arg1) {
                // TODO Auto-generated method stub
            }
        });

    }
}
