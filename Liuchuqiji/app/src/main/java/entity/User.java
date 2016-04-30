package entity;

import android.content.Context;
import android.widget.ImageView;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
/**
 * Created by dz on 2016/3/14.
 */
public class User extends BmobUser {
    private BmobFile avatar;
    private String nickname;
    private int points;
    private int winNum;
    private int allNum;
    //TODO 返回有效路径
    public String getAvatar() {
        return "";
    }
    public void loadAvatar(Context context,ImageView iv){
                if(avatar==null)
                    return;
                else
                    avatar.loadImage(context,iv);
    }
    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }
    public void setNickname(String nickname){
        this.nickname=nickname;
    }
    public String getNickname(){
     return nickname;
    }
    public void setPoints(int points){
        this.points=points;
    }
    public int getPoints() {
        return points;
    }
    public void setWinNum(int winNum){
        this.winNum=winNum;
    }
    public int getWinNum(){
        return winNum;
    }
    public void setAllNum(int allNum){
        this.allNum=allNum;
    }
    public int getAllNum(){
        return allNum;
    }
}