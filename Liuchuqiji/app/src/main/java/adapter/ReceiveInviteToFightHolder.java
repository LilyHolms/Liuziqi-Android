package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.softwareprojectmanagement.liuziqi.lily.ui.BaseActivity;
import com.softwareprojectmanagement.liuziqi.lily.ui.NetChatActivity;
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
import core.BmobIMApplication;
import entity.InViteToFightAgreeMessage;

/**
 * Created by Lily on 16/4/10.
 */
public class ReceiveInviteToFightHolder extends BaseViewHolder {


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
    public ReceiveInviteToFightHolder(Context context, ViewGroup root,BmobIMConversation c,OnRecyclerViewListener onRecyclerViewListener) {
        super(context, root, R.layout.item_chat_received_invite_message,onRecyclerViewListener);
        this.c =c;
        this.context = context;
    }

    @OnClick({R.id.iv_avatar})
    public void onAvatarClick(View view) {

    }

    @OnClick(R.id.btn_agree_fight)
    public void onAgreeFightClick(View view){
        InViteToFightAgreeMessage msg = new InViteToFightAgreeMessage();
        msg.setContent("agreeFight");
        c.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                Logger.i("othermsg:" + msg.toString());
                if (e == null) {//发送成功
                    toast("同意对战发送成功");
                } else {//发送失败
                    toast("同意对战发送失败:" + e.getMessage());
                }
            }
        });

//*************note:在非acitivity中跳转界面的方法********************
//        Activity currentActivity = (Activity) v.getContext();
//        Intent intent = new Intent(currentActivity, OtherActivity.class);
//        currentActivity.startActivity(intent);

        Bundle bundle = new Bundle();
        bundle.putSerializable("c", c);
        BaseActivity currentActivity = (BaseActivity) view.getContext();
        currentActivity.startActivity(NetFightActivity.class, bundle, false);


    }

    @OnClick(R.id.btn_reject_fight)
    public void onRejectFight(View view){
        InViteToFightAgreeMessage msg = new InViteToFightAgreeMessage();
        msg.setContent("rejectFight");
        c.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                Logger.i("othermsg:" + msg.toString());
                if (e == null) {//发送成功
                    btn_agree_fight.setEnabled(false);
                    toast("拒绝对战发送成功");
                } else {//发送失败
                    toast("拒绝对战发送失败:" + e.getMessage());
                }
            }
        });
    }

    @Override
    public void bindData(Object o) {
        final BmobIMMessage message = (BmobIMMessage)o;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = dateFormat.format(message.getCreateTime());
        tv_time.setText(time);
        final BmobIMUserInfo info = message.getBmobIMUserInfo();
//        ViewUtil.setAvatar(info != null ? info.getAvatar() : null, R.mipmap.head, iv_avatar);
        String content =  message.getContent();
        tv_message.setText(content);
        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("点击" + info.getName() + "的头像");
            }
        });

        tv_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("点击"+message.getContent());
                if(onRecyclerViewListener!=null){
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