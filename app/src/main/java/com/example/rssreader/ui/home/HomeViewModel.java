package com.example.rssreader.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.model.Feed;
import com.example.rssreader.data.model.FilterState;
import com.example.rssreader.data.repository.ArticleRepository;
import com.example.rssreader.data.repository.FeedRepository;
import com.example.rssreader.util.PrefsManager;
import com.example.rssreader.util.SessionManager;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final ArticleRepository articleRepository;
    private final FeedRepository feedRepository;
    private final SessionManager sessionManager;
    private final PrefsManager prefsManager;

    private final MutableLiveData<FilterState> filterStateLiveData = new MutableLiveData<>();
    private String lastSearchQuery = "";

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.articleRepository = new ArticleRepository(application);
        this.feedRepository = new FeedRepository(application);
        this.sessionManager = new SessionManager(application);
        this.prefsManager = new PrefsManager(application);

        FilterState initialFilter = prefsManager.getFilterState();
        filterStateLiveData.setValue(initialFilter);

        feedRepository.loadFeeds();
        loadArticles();
    }

    public LiveData<List<Article>> getArticles() {
        return articleRepository.getArticlesLiveData();
    }

    public LiveData<List<Feed>> getFeeds() {
        return feedRepository.getFeedsLiveData();
    }

    public LiveData<Boolean> getIsSyncing() {
        return articleRepository.getIsSyncingLiveData();
    }

    public LiveData<FilterState> getFilterState() {
        return filterStateLiveData;
    }

    public long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void setFilterType(FilterState.FilterType type, long feedId) {
        FilterState state = filterStateLiveData.getValue();
        if (state == null) state = new FilterState();
        state.setFilterType(type);
        state.setSelectedFeedId(feedId);
        filterStateLiveData.setValue(state);
        prefsManager.saveFilterState(state);
        loadArticles();
    }

    public void setSortOrder(FilterState.SortOrder sortOrder) {
        FilterState state = filterStateLiveData.getValue();
        if (state == null) state = new FilterState();
        state.setSortOrder(sortOrder);
        filterStateLiveData.setValue(state);
        prefsManager.saveFilterState(state);
        loadArticles();
    }

    public void search(String query) {
        this.lastSearchQuery = query;
        loadArticles();
    }

    public void loadArticles() {
        FilterState state = filterStateLiveData.getValue();
        if (state == null) state = new FilterState();
        articleRepository.loadFilteredArticles(sessionManager.getUserId(), state, lastSearchQuery);
    }

    public void refreshFeeds(ArticleRepository.SyncCallback callback) {
        articleRepository.refreshAllFeeds(sessionManager.getUserId(), "", (count, error) -> {
            loadArticles();
            if (callback != null) callback.onComplete(count, error);
        });
    }

    public void toggleBookmark(Article article, Runnable onComplete) {
        articleRepository.toggleBookmark(sessionManager.getUserId(), article, () -> {
            loadArticles();
            if (onComplete != null) onComplete.run();
        });
    }
}
