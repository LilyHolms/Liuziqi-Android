package adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.softwareprojectmanagement.liuziqi.lily.ui.BaseActivity;
import com.softwareprojectmanagement.liuziqi.lily.ui.NetFightActivity;
import com.softwareprojectmanagement.liuziqi.lily.ui.R;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import entity.AddFriendReplyMessage;
import entity.Friends;
import entity.InViteToFightAgreeMessage;
import entity.User;
import model.UserModel;

/**
 * Created by Lily on 16/4/22.
 */
public class ReiceiveAddFriendHolder extends BaseViewHolder {


    @Bind(R.id.iv_avatar)
    protected ImageView iv_avatar;

    @Bind(R.id.tv_time)
    protected TextView tv_time;

    @Bind(R.id.tv_message)
    protected TextView tv_message;

    @Bind(R.id.btn_agree_fight)
    protected Button btn_agree_fight;

    @Bind(R.id.btn_reject_fight)
    protected Button btn_reject_fight;

    BmobIMConversation c;
    Context context;

    public ReiceiveAddFriendHolder(Context context, ViewGroup root, BmobIMConversation c, OnRecyclerViewListener onRecyclerViewListener) {
        super(context, root, R.layout.item_chat_received_invite_message, onRecyclerViewListener);
        this.c = c;
        this.context = context;
        btn_agree_fight.setText("同意");
    }

    @OnClick({R.id.iv_avatar})
    public void onAvatarClick(View view) {

    }

    @OnClick(R.id.btn_agree_fight)
    public void onAgreeFriendClick(View view) {//同意加好友
        AddFriendReplyMessage msg = new AddFriendReplyMessage();
        msg.setContent("agreeFriend");
        c.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                Logger.i("othermsg:" + msg.toString());
                if (e == null) {//发送成功
                    toast("发送成功");
                    Friends friends = new Friends();

                    //好友信息加入数据库,对两人username进行比较,较小的字符串摆前面
                    String friend_A = UserModel.getInstance().getCurrentUser().getUsername();
                    String friend_B = c.getConversationTitle();
                    if( friend_A.compareTo(friend_B)<0 ){
                        friends.setId(friend_A);
                        friends.setFriendId(friend_B);
                    }else{
                        friends.setId(friend_B);
                        friends.setFriendId(friend_A);
                    }
                    friends.save(getContext(), new SaveListener() {

                        @Override
                        public void onSuccess() {
                            toast("添加数据成功");
                        }

                        @Override
                        public void onFailure(int code, String arg0) {
                            toast("添加数据失败");
                        }
                    });
                } else {//发送失败
                    toast("发送失败:" + e.getMessage());
                }
            }
        });
    }

    @OnClick(R.id.btn_reject_fight)
    public void onRejectFriend(View view) {//拒绝加好友
        AddFriendReplyMessage msg = new AddFriendReplyMessage();
        msg.setContent("rejectFriend");
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

    @Override
    public void bindData(Object o) {
        final BmobIMMessage message = (BmobIMMessage) o;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = dateFormat.format(message.getCreateTime());
        tv_time.setText(time);
        final BmobIMUserInfo info = message.getBmobIMUserInfo();
//        ViewUtil.setAvatar(info != null ? info.getAvatar() : null, R.mipmap.head, iv_avatar);
//        String content = message.getContent();
        tv_message.setText(c.getConversationTitle()+"申请添加您为好友");
        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("点击" + info.getName() + "的头像");
            }
        });

        tv_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("点击" + message.getContent());
                if (onRecyclerViewListener != null) {
                    onRecyclerViewListener.onItemClick(getAdapterPosition());
                }
            }
        });

        tv_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onRecyclerViewListener != null) {
                    onRecyclerViewListener.onItemLongClick(getAdapterPosition());
                }
                return true;
            }
        });

    }

    public void showTime(boolean isShow) {
        tv_time.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }


}
