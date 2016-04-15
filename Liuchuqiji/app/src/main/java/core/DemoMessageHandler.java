package core;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.softwareprojectmanagement.liuziqi.lily.ui.MainActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.exception.BmobException;
import model.UserModel;
import model.i.UpdateCacheListener;

/**
 * Created by Lily on 16/3/22.
 * 1.子线程，利用handle发送消息放在主线程的消息队列中
 * 2.主线程里面有一个looper循环获取消息
 * 3.发现新的消息，就会调用onMessageReceive方法处理消息
 */

public class DemoMessageHandler extends BmobIMMessageHandler{

    private Context context;

    public DemoMessageHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onMessageReceive(final MessageEvent event) {
        //当接收到服务器发来的消息时，此方法被调用
        Logger.i(event.getConversation().getConversationTitle() + "," + event.getMessage().getMsgType() + "," + event.getMessage().getContent());
        //检测更新下用户的信息
        UserModel.getInstance().updateUserInfo(event, new UpdateCacheListener() {
            @Override
            public void done(BmobException e) {
                BmobIMMessage msg = event.getMessage();
//                if(BmobIMMessageType.getMessageTypeValue(msg.getMsgType())==0){//用户自定义的消息类型，其类型值均为0
//                    //自行处理自定义消息类型
//                    Logger.i(msg.getMsgType() + "," + msg.getContent() + "," + msg.getExtra());
//                    Toast.makeText(context, msg.getMsgType() + "," + msg.getContent(), Toast.LENGTH_SHORT).show();
//                    EventBus.getDefault().post(event);//lily
//                } else{
            //SDK内部内部支持的消息类型
                    if (BmobNotificationManager.getInstance(context).isShowNotification()){//如果需要显示通知栏，SDK提供以下两种显示方式：
                        Logger.i(msg.getMsgType() + "," + msg.getContent() + "," + msg.getExtra());
                        Toast.makeText(context, msg.getMsgType() + "," + msg.getContent(), Toast.LENGTH_SHORT).show();

                        Intent pendingIntent = new Intent(context, MainActivity.class);
                        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        //1、多个用户的多条消息合并成一条通知：有XX个联系人发来了XX条消息
                        BmobNotificationManager.getInstance(context).showNotification(event, pendingIntent);
                        //2、自定义通知消息：始终只有一条通知，新消息覆盖旧消息
//                        BmobIMMessage msg =event.getMessage();
//                        BmobIMUserInfo info =event.getFromUserInfo();
//                        //这里可以是应用图标，也可以将聊天头像转成bitmap
//                        Bitmap largetIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
//                        BmobNotificationManager.getInstance().showNotification(largetIcon,
//                                info.getName(),msg.getContent(),"您有一条新消息",pendingIntent);
                    }else{//直接发送消息事件
                        Logger.i("当前处于应用内，发送event");
                        EventBus.getDefault().post(event);
                    }
//                }
            }
        });
    }

    @Override
    public void onOfflineReceive(final OfflineMessageEvent event) {
        //每次调用connect方法时会查询一次离线消息，如果有，此方法会被调用
        Map<String,List<MessageEvent>> map =event.getEventMap();
        Logger.i("离线消息属于"+map.size()+"个用户");
        for (Map.Entry<String, List<MessageEvent>> entry : map.entrySet()) {
            List<MessageEvent> list =entry.getValue();
            //挨个检测离线用户信息是否需要更新
            UserModel.getInstance().updateUserInfo(list.get(0), new UpdateCacheListener() {
                @Override
                public void done(BmobException e) {
                    EventBus.getDefault().post(event);
                }
            });
        }
    }
}