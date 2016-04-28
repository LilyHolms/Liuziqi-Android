package com.softwareprojectmanagement.liuziqi.lily.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.ValueEventListener;
import entity.ChatMessage;
import entity.User;
import model.UserModel;

public class ChatRoomActivity extends Activity implements OnClickListener  {
    ListView lv_data;
    Button btn_send;
    EditText et_name, et_content;

    MyAdapter myAdapter;
    List<ChatMessage> messages = new ArrayList<ChatMessage>();
    BmobRealTimeData data = new BmobRealTimeData();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        init();
        //et_name = (EditText) findViewById(R.id.et_name);
        et_content = (EditText) findViewById(R.id.et_content);
        lv_data = (ListView) findViewById(R.id.lv_data);

        myAdapter = new MyAdapter();
        lv_data.setAdapter(myAdapter);
    }
    private void init(){
        Toast.makeText(this, "聊天大厅", Toast.LENGTH_SHORT).show();
        //Bmob.initialize(this, "你的appkey");
        data.start(this, new ValueEventListener() {//连接服务器
            public void onDataChange(JSONObject arg0) {//数据改变回调函数
                //在这里判断一下是不是自己发送的消息，选择对应的ui
                Logger.i("数据改变 success");
                if (BmobRealTimeData.ACTION_UPDATETABLE.equals(arg0.optString("action"))) {
                    JSONObject data = arg0.optJSONObject("data");
                    messages.add(new ChatMessage(data.optString("UserObjectId"),data.optString("name"), data.optString("content")));//收到信息
                    myAdapter.notifyDataSetChanged();
                }
            }

            public void onConnectCompleted() {
                // Log.d("bmob", "连接成功:" + match_data.isConnected());
                Logger.i("连接成功 success");
                if (data.isConnected()) {
                    data.subTableUpdate("ChatMessage");//监听数据改变
                }
            }
        });
    }
    @Override
    public void onClick(View v) {//点击发送
        // TODO Auto-generated method stub
        // String name = et_name.getText().toString();

        String content = et_content.getText().toString();
        if( TextUtils.isEmpty(content)){
            Toast.makeText(this, "用户名和内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }else{
            sendMsg( content);
        }
    }
    private void sendMsg(String msg){
        Toast.makeText(this, "发送", Toast.LENGTH_SHORT).show();
        User user = UserModel.getInstance().getCurrentUser();//获取当前用户
        String name = user.getUsername();
        String UserObjectId = user.getObjectId();
        final ChatMessage chat = new ChatMessage(UserObjectId,name, msg);
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

    private class MyAdapter extends BaseAdapter{

        ViewHolder holder;
        private final int TYPE_RECEIVER_TXT = 0;
        private final int TYPE_SEND_TXT = 1;
        public int getItemViewType(int position) {
            ChatMessage chat = messages.get(position);
            User user = UserModel.getInstance().getCurrentUser();//获取当前用户
            Logger.i("1:" + chat.getUserObjectId());
            Logger.i("2:" + user.getObjectId());
            return (chat.getUserObjectId().equals(user.getObjectId()))?TYPE_SEND_TXT:TYPE_RECEIVER_TXT;

        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {//更新list
            int x = getItemViewType(position);
            if(x==0) Logger.i("0");
            if(x==0) {

                // TODO Auto-generated method stub
                if (convertView == null) {
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_chat_received_message, null);
                    holder = new ViewHolder();

                    holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
                    holder.tv_content = (TextView) convertView.findViewById(R.id.tv_message);
                    holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
            }
            else{
                if (convertView == null) {
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_chat_sent_message, null);
                    holder = new ViewHolder();

                    holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
                    holder.tv_content = (TextView) convertView.findViewById(R.id.tv_message);
                    holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

            }
            ChatMessage chat = messages.get(position);
            //holder.avatar.set;
            holder.tv_content.setText(chat.getContent());
            holder.tv_time.setText(chat.getCreatedAt());

            return convertView;
        }

        class ViewHolder{
            ImageView avatar;
            TextView tv_content;
            TextView tv_time;
        }
    }
}
