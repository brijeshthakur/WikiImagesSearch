package com.android.vaibhav.imagessearch;

/**
 * Created by Vaibhav on 11/19/2015.
 */
public class SearchResult {
    private String id;
    private String title;
    private String source;

    public SearchResult(String id, String title, String source) {
        this.id = id;
        this.title = title;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean hasThumbnail() {
        return !source.isEmpty();
    }
}
