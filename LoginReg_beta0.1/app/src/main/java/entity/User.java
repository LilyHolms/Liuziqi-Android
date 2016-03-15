package entity;

import cn.bmob.v3.BmobUser;

/**
 * Created by dz on 2016/3/14.
 */
public class User extends BmobUser {
    private String info;

    public String getInfo(){
        return this.info;
    }
    public void setInfo(String info){
        this.info=info;
    }
}
