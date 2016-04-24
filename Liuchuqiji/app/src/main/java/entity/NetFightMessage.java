package entity;

import cn.bmob.newim.bean.BmobIMExtraMessage;

/**
 * Created by Lily on 16/4/8.
 */
public class NetFightMessage extends BmobIMExtraMessage {

    public NetFightMessage(){
    }

    @Override
    public String getMsgType() {
        return "chess";
    }

    @Override
    public boolean isTransient() {
        //设置为true,表明为暂态消息，那么这条消息并不会保存到本地db中，SDK只负责发送出去
        //设置为false,则会保存到指定会话的数据库中
        return true;
    }


}