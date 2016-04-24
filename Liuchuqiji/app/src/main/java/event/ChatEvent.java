package event;

import cn.bmob.newim.bean.BmobIMUserInfo;

/**
 * Created by Lily on 16/4/6.
 */
public class ChatEvent {

    public BmobIMUserInfo info;

    public ChatEvent(BmobIMUserInfo info){
        this.info=info;
    }
}
