package com.example.appctruyn.model;

public class LibraryStory {
    private String storyId = "";
    private String title = "";
    private String coverUrl = "";
    private int lastChap = 0;
    private int totalChap = 0;
    private boolean notifyEnabled = false;

    public LibraryStory() {
    }

    public LibraryStory(String storyId, String title, String coverUrl, int lastChap, int totalChap, boolean notifyEnabled) {
        this.storyId = storyId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.lastChap = lastChap;
        this.totalChap = totalChap;
        this.notifyEnabled = notifyEnabled;
    }

    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public int getLastChap() { return lastChap; }
    public void setLastChap(int lastChap) { this.lastChap = lastChap; }

    public int getTotalChap() { return totalChap; }
    public void setTotalChap(int totalChap) { this.totalChap = totalChap; }

    public boolean isNotifyEnabled() { return notifyEnabled; }
    public void setNotifyEnabled(boolean notifyEnabled) { this.notifyEnabled = notifyEnabled; }
}
