package entity;

import cn.bmob.v3.BmobObject;
//白旗在线表
/**
 * Created by camellia on 16/4/26.
 */

public class WhiteChessQueue  extends BmobObject {
    private String name;
    private String UserObjectId;
    private int chooseTime;
    private String SysTime;
    private String UserObjectId2;
    //otherflag
    public WhiteChessQueue(){}
    public WhiteChessQueue(String name, String UserObjectId,int chooseTime,String SysTime){
        this.name = name;
        this.UserObjectId = UserObjectId;
        this.chooseTime = chooseTime;
        this.SysTime = SysTime;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUserObjectId() {
        return UserObjectId;
    }
    public String getUserObjectId2() {
        return UserObjectId2;
    }
    public void setUserObjectId(String UserObjectId) {
        this.UserObjectId = UserObjectId;
    }
    public void setUserObjectId2(String UserObjectId) {
        this.UserObjectId2 = UserObjectId;
    }

    public void setChooseTime(int time) {
        this.chooseTime = time;
    }
    public int getChooseTime() {
        return chooseTime;
    }
    public void setSysTime(String SysTime) {
        this.SysTime = SysTime;
    }
    public String getSysTime() {
        return SysTime;
    }

}
