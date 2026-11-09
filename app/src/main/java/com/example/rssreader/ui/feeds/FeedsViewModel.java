package com.example.rssreader.ui.feeds;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.rssreader.data.model.Feed;
import com.example.rssreader.data.repository.FeedRepository;

import java.util.List;

public class FeedsViewModel extends AndroidViewModel {

    private final FeedRepository feedRepository;

    public FeedsViewModel(@NonNull Application application) {
        super(application);
        this.feedRepository = new FeedRepository(application);
        loadFeeds();
    }

    public LiveData<List<Feed>> getFeeds() {
        return feedRepository.getFeedsLiveData();
    }

    public void loadFeeds() {
        feedRepository.loadFeeds();
    }

    public void addFeed(String title, String url, String category, Runnable onComplete) {
        feedRepository.addFeed(title, url, category, onComplete);
    }

    public void setFeedEnabled(long feedId, boolean isEnabled) {
        feedRepository.setFeedEnabled(feedId, isEnabled);
    }

    public void deleteFeed(long feedId) {
        feedRepository.deleteFeed(feedId);
    }
}
