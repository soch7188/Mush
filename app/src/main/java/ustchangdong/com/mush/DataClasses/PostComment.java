package ustchangdong.com.mush.DataClasses;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ziwon on 2017. 10. 2..
 */

public class PostComment {

    private String userid;
    private String content;
    private double timestamp;

    public PostComment() {
    }

    public PostComment(String userid, String content, Double timestamp) {
        this.userid = userid;
        this.content = content;
        this.timestamp = timestamp;
    }

    public double getTimestamp(){
        return timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userid", userid);
        result.put("content", content);
        result.put("timestamp", ServerValue.TIMESTAMP);
        return result;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }
}
