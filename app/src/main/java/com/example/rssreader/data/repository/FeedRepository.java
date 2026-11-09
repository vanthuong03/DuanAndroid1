package com.example.rssreader.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rssreader.data.local.DatabaseHelper;
import com.example.rssreader.data.local.FeedDao;
import com.example.rssreader.data.model.Feed;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedRepository {

    private final FeedDao feedDao;
    private final ExecutorService executor;
    private final MutableLiveData<List<Feed>> feedsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> categoriesLiveData = new MutableLiveData<>();

    public FeedRepository(Context context) {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        this.feedDao = new FeedDao(helper);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Feed>> getFeedsLiveData() {
        return feedsLiveData;
    }

    public LiveData<List<String>> getCategoriesLiveData() {
        return categoriesLiveData;
    }

    public void loadFeeds() {
        executor.execute(() -> {
            List<Feed> list = feedDao.getAllFeeds();
            feedsLiveData.postValue(list);

            List<String> categories = feedDao.getCategories();
            categoriesLiveData.postValue(categories);
        });
    }

    public void addFeed(String title, String url, String category, Runnable onComplete) {
        executor.execute(() -> {
            Feed feed = new Feed(title, url, "", category, true);
            feedDao.insert(feed);
            loadFeeds();
            if (onComplete != null) onComplete.run();
        });
    }

    public void setFeedEnabled(long feedId, boolean isEnabled) {
        executor.execute(() -> {
            feedDao.setFeedEnabled(feedId, isEnabled);
            loadFeeds();
        });
    }

    public void deleteFeed(long feedId) {
        executor.execute(() -> {
            feedDao.delete(feedId);
            loadFeeds();
        });
    }
}
