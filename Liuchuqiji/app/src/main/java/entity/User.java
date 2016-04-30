package entity;

import cn.bmob.v3.BmobUser;
/**
 * Created by dz on 2016/3/14.
 */
public class User extends BmobUser {
    private String avatar;
    //private String nickname;
    //private int score;
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
//    public void setNickname(String nickname){
//        this.nickname=nickname;
//    }
//    public String getNickname(){
//     return nickname;
//    }
//    public void setScore(int score){
//        this.score=score;
//    }
//    public int getScore() {
//        return score;
//    }
//
}