package ustchangdong.com.mush;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ziwon on 2017. 10. 2..
 */

public class Post {

    private String fbdbid;
    private String userid;
    private String title;
    private String content;
    private double timestamp;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String fbdbid, String userId, String title, String content) {
        this.fbdbid = fbdbid;
        this.userid = userId;
        this.title = title;
        this.content = content;
    }

    public double getTimestamp(){
        return timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fbdbid", fbdbid);
        result.put("userid", userid);
        result.put("title", title);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getFbdbid() {
        return fbdbid;
    }

    public void setFbdbid(String fbdbid) {
        this.fbdbid = fbdbid;
    }
}
