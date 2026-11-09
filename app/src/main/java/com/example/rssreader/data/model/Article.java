package com.example.rssreader.data.model;

import java.io.Serializable;
import java.util.Objects;

public class Article implements Serializable {
    private long id;
    private long feedId;
    private String title;
    private String link;
    private String description;
    private String imageUrl;
    private long pubDate;
    private boolean isRead;
    private String content;

    // Transient/Helper fields for UI display
    private boolean isBookmarked;
    private String feedTitle;

    public Article() {}

    public Article(long feedId, String title, String link, String description, String imageUrl, long pubDate, boolean isRead) {
        this.feedId = feedId;
        this.title = title;
        this.link = link;
        this.description = description;
        this.imageUrl = imageUrl;
        this.pubDate = pubDate;
        this.isRead = isRead;
    }

    public Article(long id, long feedId, String title, String link, String description, String imageUrl, long pubDate, boolean isRead) {
        this.id = id;
        this.feedId = feedId;
        this.title = title;
        this.link = link;
        this.description = description;
        this.imageUrl = imageUrl;
        this.pubDate = pubDate;
        this.isRead = isRead;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getFeedId() { return feedId; }
    public void setFeedId(long feedId) { this.feedId = feedId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getPubDate() { return pubDate; }
    public void setPubDate(long pubDate) { this.pubDate = pubDate; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isBookmarked() { return isBookmarked; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    public String getFeedTitle() { return feedTitle; }
    public void setFeedTitle(String feedTitle) { this.feedTitle = feedTitle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return id == article.id && feedId == article.feedId &&
                pubDate == article.pubDate && isRead == article.isRead &&
                isBookmarked == article.isBookmarked &&
                Objects.equals(title, article.title) &&
                Objects.equals(link, article.link) &&
                Objects.equals(description, article.description) &&
                Objects.equals(imageUrl, article.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, feedId, title, link, description, imageUrl, pubDate, isRead, isBookmarked);
    }
}
