package com.example.rssreader.data.model;

import java.io.Serializable;
import java.util.Objects;

public class Feed implements Serializable {
    private long id;
    private String title;
    private String url;
    private String iconUrl;
    private String category;
    private boolean isEnabled;

    public Feed() {
        this.isEnabled = true;
    }

    public Feed(String title, String url, String iconUrl, String category, boolean isEnabled) {
        this.title = title;
        this.url = url;
        this.iconUrl = iconUrl;
        this.category = category;
        this.isEnabled = isEnabled;
    }

    public Feed(long id, String title, String url, String iconUrl, String category, boolean isEnabled) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.iconUrl = iconUrl;
        this.category = category;
        this.isEnabled = isEnabled;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return id == feed.id && isEnabled == feed.isEnabled &&
                Objects.equals(title, feed.title) &&
                Objects.equals(url, feed.url) &&
                Objects.equals(category, feed.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, url, category, isEnabled);
    }
}
