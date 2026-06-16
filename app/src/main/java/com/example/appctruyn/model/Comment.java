package com.example.appctruyn.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String content;
    @ServerTimestamp
    private Date timestamp;
    private String storyId;
    private int chapterNumber;

    public Comment() {}

    public Comment(String userId, String userName, String content, String storyId, int chapterNumber) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.storyId = storyId;
        this.chapterNumber = chapterNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }
    public int getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(int chapterNumber) { this.chapterNumber = chapterNumber; }
}
