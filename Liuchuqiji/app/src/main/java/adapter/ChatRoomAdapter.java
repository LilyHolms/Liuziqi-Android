package adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.softwareprojectmanagement.liuziqi.lily.ui.R;

import java.util.List;

import entity.ChatMessage;
import entity.User;
import model.UserModel;

/**
 * Created by camellia on 16/4/30.
 */
public class ChatRoomAdapter extends  RecyclerView.Adapter {

    // ViewHolder holder;
    private final int TYPE_RECEIVER_TXT = 0;
    private final int TYPE_SEND_TXT = 1;

    public static interface OnRecyclerViewListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
                this.onRecyclerViewListener = onRecyclerViewListener;
    }

    private static final String TAG = ChatRoomAdapter.class.getSimpleName();
    private List<ChatMessage> messages;
    public ChatRoomAdapter(List<ChatMessage> list) {
        this.messages = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Logger.i("ViewHolder" + viewType);
        View view;
        if (viewType == TYPE_SEND_TXT) {
             view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_sent_message, null);
            //return new SendTextHolder(parent.getContext(), parent,c,onRecyclerViewListener);
        }
        else{
             view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_received_message, null);
            //return new ReceiveTextHolder(parent.getContext(), parent,onRecyclerViewListener);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new MsgViewHolder(view,viewType);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        MsgViewHolder holder = (MsgViewHolder) viewHolder;
        holder.position = i;
        ChatMessage chatMsg = messages.get(i);
        holder.tv_content.setText(chatMsg.getContent());
        holder.tv_time.setText(chatMsg.getCreateTime());

    }
    @Override
    public int getItemCount() {
        return messages.size();
    }
    @Override
    public int getItemViewType(int position) {
        ChatMessage chat = messages.get(position);
        User user = UserModel.getInstance().getCurrentUser();//获取当前用户
        return (chat.getUserObjectId().equals(user.getObjectId())) ? TYPE_SEND_TXT : TYPE_RECEIVER_TXT;

    }


    class MsgViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public View rootView;
        ImageView avatar;
        TextView tv_content;
        TextView tv_time;


        public int position;

        public MsgViewHolder(View itemView,int viewType) {
            super(itemView);
            tv_content = (TextView) itemView.findViewById(R.id.tv_message);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            avatar = (ImageView)  itemView.findViewById(R.id.iv_avatar);

            if(viewType == TYPE_SEND_TXT) {
                ProgressBar progress_load;
                progress_load = (ProgressBar)itemView.findViewById(R.id.progress_load);
                progress_load.setVisibility(View.GONE);
                rootView = itemView.findViewById(R.id.recycler_view_sent_item);
            }
            else{
                rootView = itemView.findViewById(R.id.recycler_view_received_item);
            }
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (null != onRecyclerViewListener) {
                onRecyclerViewListener.onItemClick(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(null != onRecyclerViewListener){
                return onRecyclerViewListener.onItemLongClick(position);
            }
            return false;
        }
    }

}


