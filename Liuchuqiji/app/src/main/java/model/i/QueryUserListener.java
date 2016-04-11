package model.i;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.BmobListener;
import entity.User;

/**
 * Created by Lily on 16/3/23.
 */
public abstract class QueryUserListener extends BmobListener<User> {
    public abstract void done(User s, BmobException e);

    @Override
    protected void postDone(User o, BmobException e) {
        done(o, e);
    }
}