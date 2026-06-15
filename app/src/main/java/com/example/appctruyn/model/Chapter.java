package com.example.appctruyn.model;

import com.google.firebase.firestore.DocumentId;

public class Chapter {
    @DocumentId 
    private String id = "";
    private String title = "";
    private int number = 0;
    private int chapterNumber = 0;
    private String content = "";
    private String storyId = "";
    private String timestamp = "";

    public Chapter() {
    }

    public Chapter(String id, String title, int number, int chapterNumber, String content, String storyId, String timestamp) {
        this.id = id;
        this.title = title;
        this.number = number;
        this.chapterNumber = chapterNumber;
        this.content = content;
        this.storyId = storyId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public int getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(int chapterNumber) { this.chapterNumber = chapterNumber; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
