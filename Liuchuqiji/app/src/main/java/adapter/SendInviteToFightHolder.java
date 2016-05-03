package adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.softwareprojectmanagement.liuziqi.lily.ui.BaseActivity;
import com.softwareprojectmanagement.liuziqi.lily.ui.NetChatActivity;
import com.softwareprojectmanagement.liuziqi.lily.ui.R;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMSendStatus;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.exception.BmobException;
import entity.InViteToFightWithdrawMessage;
import model.UserModel;

/**
 * Created by Lily on 16/4/10.
 */
public class SendInviteToFightHolder extends BaseViewHolder implements View.OnClickListener,View.OnLongClickListener {

    @Bind(R.id.iv_avatar)
    protected ImageView iv_avatar;

    @Bind(R.id.iv_fail_resend)
    protected ImageView iv_fail_resend;

    @Bind(R.id.tv_time)
    protected TextView tv_time;

    @Bind(R.id.tv_message)
    protected TextView tv_message;
    @Bind(R.id.tv_send_status)
    protected TextView tv_send_status;

    @Bind(R.id.progress_load)
    protected ProgressBar progress_load;

    @Bind(R.id.btn_withdraw_invite)
    protected Button btn_withdraw_invite;

    BmobIMConversation c;

    public SendInviteToFightHolder(Context context, ViewGroup root,BmobIMConversation c,OnRecyclerViewListener listener) {
        super(context, root, R.layout.item_chat_sent_invite_message, listener);
        this.c =c;
        UserModel.getInstance().loadAvatar(context, iv_avatar);//头像
    }

    @Override
    public void bindData(Object o) {
        final BmobIMMessage message = (BmobIMMessage)o;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final BmobIMUserInfo info = message.getBmobIMUserInfo();
        String time = dateFormat.format(message.getCreateTime());
        String content = message.getContent();
        tv_message.setText(content);
        tv_time.setText(time);

        int status =message.getSendStatus();
        if (status == BmobIMSendStatus.SENDFAILED.getStatus()) {
            iv_fail_resend.setVisibility(View.VISIBLE);
            progress_load.setVisibility(View.GONE);
        } else if (status== BmobIMSendStatus.SENDING.getStatus()) {
            iv_fail_resend.setVisibility(View.GONE);
            progress_load.setVisibility(View.VISIBLE);
        } else {
            iv_fail_resend.setVisibility(View.GONE);
            progress_load.setVisibility(View.GONE);
        }

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

        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("点击" + info.getName() + "的头像");
            }
        });

        //重发
        iv_fail_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.resendMessage(message, new MessageSendListener() {
                    @Override
                    public void onStart(BmobIMMessage msg) {
                        progress_load.setVisibility(View.VISIBLE);
                        iv_fail_resend.setVisibility(View.GONE);
                        tv_send_status.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void done(BmobIMMessage msg, BmobException e) {
                        if(e==null){
                            tv_send_status.setVisibility(View.VISIBLE);
                            tv_send_status.setText("已发送");
                            iv_fail_resend.setVisibility(View.GONE);
                            progress_load.setVisibility(View.GONE);
                        }else{
                            iv_fail_resend.setVisibility(View.VISIBLE);
                            progress_load.setVisibility(View.GONE);
                            tv_send_status.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });


        btn_withdraw_invite.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                c.deleteMessage(message);//message是要被撤回的消息,msg是通知对方撤回该消息的消息

                InViteToFightWithdrawMessage msg = new InViteToFightWithdrawMessage();
                msg.setContent("withdrawFight");
                c.sendMessage(msg, new MessageSendListener() {
                    @Override
                    public void done(BmobIMMessage msg, BmobException e) {
                        Logger.i("othermsg:" + msg.toString());
                        if (e == null) {//发送成功
                            toast("撤回对战发送成功");
                        } else {//发送失败
                            toast("撤回对战发送失败:" + e.getMessage());
                        }
                    }
                });

                //TODO:这里没有想到很好的方式向activity发消息,更新isWaiting值.所以暂时写成刷新界面
                //被迫使用了这种极其不优雅的方式,跳转界面的第三个参数暂时设置成false,点击返回按钮会变成没有撤回的样子=-=
                Bundle bundle = new Bundle();
                bundle.putSerializable("c", c);
                BaseActivity currentActivity = (BaseActivity) view.getContext();
                currentActivity.startActivity(NetChatActivity.class, bundle, false);
            }
        });
    }

    public void showTime(boolean isShow) {
        tv_time.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

}
