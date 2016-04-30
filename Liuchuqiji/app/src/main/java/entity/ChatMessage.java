package entity;
import cn.bmob.v3.BmobObject;

/**
 * Created by camellia on 16/4/26.
 */
public class ChatMessage extends BmobObject {
    private String name;
    private String content;
    private String avatar;
    private String UserObjectId;
    private String time;
    public ChatMessage(String UserObjectId, String name, String content){
        this.UserObjectId = UserObjectId;
        this.name = name;
        this.content = content;
        this.time = time;
    }

    public String getName() {
        return name;
    }
    public String getcreatetime() {
        return time;
    }
    public void setcreatetime(String time) {
         this.time = time;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.content = content;
    }
    public String getUserObjectId() {
        return UserObjectId;
    }
    public void setUserObjectId(String UserObjectId) {
        this.UserObjectId = UserObjectId;
    }
}
