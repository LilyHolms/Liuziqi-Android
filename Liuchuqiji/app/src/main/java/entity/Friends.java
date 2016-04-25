package entity;

import cn.bmob.v3.BmobObject;

/**
 * Created by Lily on 16/4/24.
 */
public class Friends extends BmobObject {
    private String id;
    private String friendId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
