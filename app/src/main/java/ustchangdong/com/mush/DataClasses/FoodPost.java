package ustchangdong.com.mush.DataClasses;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ziwon on 2017. 10. 2..
 *
 */

public class FoodPost {

    private String fbdbid;
    private String userid;
    private String title;
    private String content;
    private String comment;
    private double timestamp;
    private boolean hasImage = false;

    public FoodPost() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public FoodPost(String fbdbid, String userId, String title, String content, String comment, boolean hasImage) {
        this.fbdbid = fbdbid;
        this.userid = userId;
        this.title = title;
        this.content = content;
        this.comment = comment;
        this.hasImage = hasImage;
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
        result.put("comment", comment);
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("hasImage", hasImage);
        return result;
    }

    public String getFbdbid() {
        return fbdbid;
    }

    public void setFbdbid(String fbdbid) {
        this.fbdbid = fbdbid;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }
}
