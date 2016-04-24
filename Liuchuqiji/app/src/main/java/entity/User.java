package entity;

import cn.bmob.v3.BmobUser;

/**
 * Created by dz on 2016/3/14.
 */
public class User extends BmobUser {
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}