package com.example.appctruyn.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Review {
    private String id;
    private String userId;
    private String userName;
    private String content;
    private float rating;
    @ServerTimestamp
    private Date timestamp;
    private String storyId;

    public Review() {}

    public Review(String userId, String userName, String content, float rating, String storyId) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.rating = rating;
        this.storyId = storyId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }
}
