package com.example.appctruyn.model;

import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;

public class Story {
    @DocumentId 
    private String id = "";
    private String title = "";
    private String author = "";
    private String description = "";
    private String coverUrl = "";
    private String genre = "";
    private String status = "";
    private int chap = 0;
    private int totalChapters = 0;
    private int views = 0;
    private float rating = 0f;
    private boolean isHot = false;
    private List<Chapter> chapters = new ArrayList<>();

    public Story() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getChap() { return chap; }
    public void setChap(int chap) { this.chap = chap; }

    public int getTotalChapters() { return totalChapters; }
    public void setTotalChapters(int totalChapters) { this.totalChapters = totalChapters; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public boolean isHot() { return isHot; }
    public void setHot(boolean hot) { isHot = hot; }

    public List<Chapter> getChapters() { return chapters; }
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }
}
