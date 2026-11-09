package com.example.rssreader.data.model;

import java.io.Serializable;

public class FilterState implements Serializable {

    public enum FilterType {
        ALL,
        UNREAD,
        BY_FEED
    }

    public enum SortOrder {
        NEWEST,
        OLDEST,
        FEED_A_TO_Z
    }

    private FilterType filterType = FilterType.ALL;
    private long selectedFeedId = -1;
    private SortOrder sortOrder = SortOrder.NEWEST;

    public FilterState() {}

    public FilterState(FilterType filterType, long selectedFeedId, SortOrder sortOrder) {
        this.filterType = filterType;
        this.selectedFeedId = selectedFeedId;
        this.sortOrder = sortOrder;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public long getSelectedFeedId() {
        return selectedFeedId;
    }

    public void setSelectedFeedId(long selectedFeedId) {
        this.selectedFeedId = selectedFeedId;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }
}
