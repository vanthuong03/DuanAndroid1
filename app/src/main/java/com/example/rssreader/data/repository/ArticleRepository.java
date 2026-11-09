package com.example.rssreader.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rssreader.data.local.ArticleDao;
import com.example.rssreader.data.local.BookmarkDao;
import com.example.rssreader.data.local.DatabaseHelper;
import com.example.rssreader.data.local.FeedDao;
import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.model.Feed;
import com.example.rssreader.data.remote.RssFetcher;
import com.example.rssreader.data.remote.RssParser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArticleRepository {
    private static final String TAG = "ArticleRepository";

    public interface SyncCallback {
        void onComplete(int newArticlesCount, String errorMsg);
    }

    private final ArticleDao articleDao;
    private final FeedDao feedDao;
    private final BookmarkDao bookmarkDao;
    private final RssFetcher rssFetcher;
    private final RssParser rssParser;
    private final ExecutorService executor;

    private final MutableLiveData<List<Article>> articlesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Article>> bookmarkedArticlesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSyncingLiveData = new MutableLiveData<>(false);

    public ArticleRepository(Context context) {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        this.articleDao = new ArticleDao(helper);
        this.feedDao = new FeedDao(helper);
        this.bookmarkDao = new BookmarkDao(helper);
        this.rssFetcher = new RssFetcher();
        this.rssParser = new RssParser();
        this.executor = Executors.newFixedThreadPool(3);
    }

    public LiveData<List<Article>> getArticlesLiveData() {
        return articlesLiveData;
    }

    public LiveData<List<Article>> getBookmarkedArticlesLiveData() {
        return bookmarkedArticlesLiveData;
    }

    public LiveData<Boolean> getIsSyncingLiveData() {
        return isSyncingLiveData;
    }

    public void loadArticles(long currentUserId, String category) {
        executor.execute(() -> {
            List<Article> list;
            if (category == null || category.isEmpty() || "Tất cả".equalsIgnoreCase(category)) {
                list = articleDao.getAllArticles(currentUserId);
            } else {
                list = articleDao.getArticlesByCategory(category, currentUserId);
            }
            articlesLiveData.postValue(list);
        });
    }

    public void loadFilteredArticles(long currentUserId, com.example.rssreader.data.model.FilterState filterState, String query) {
        executor.execute(() -> {
            List<Article> list = articleDao.queryArticles(filterState, currentUserId, query);
            articlesLiveData.postValue(list);
        });
    }

    public void saveArticleContent(long articleId, String content) {
        executor.execute(() -> articleDao.updateContent(articleId, content));
    }

    public String getArticleContent(long articleId) {
        return articleDao.getContent(articleId);
    }

    public void searchArticles(String query, long currentUserId) {
        executor.execute(() -> {
            List<Article> list = articleDao.searchArticles(query, currentUserId);
            articlesLiveData.postValue(list);
        });
    }

    public void loadBookmarkedArticles(long currentUserId) {
        executor.execute(() -> {
            List<Article> list = bookmarkDao.getBookmarkedArticles(currentUserId);
            bookmarkedArticlesLiveData.postValue(list);
        });
    }

    public void refreshAllFeeds(long currentUserId, String currentCategory, SyncCallback callback) {
        isSyncingLiveData.postValue(true);
        executor.execute(() -> {
            int totalInserted = 0;
            String error = null;
            List<Feed> enabledFeeds = feedDao.getEnabledFeeds();

            if (enabledFeeds.isEmpty()) {
                isSyncingLiveData.postValue(false);
                if (callback != null) callback.onComplete(0, "Chưa bật nguồn tin nào");
                return;
            }

            for (Feed feed : enabledFeeds) {
                try {
                    String xml = rssFetcher.fetchXml(feed.getUrl());
                    List<Article> parsed = rssParser.parse(xml, feed.getId());
                    if (!parsed.isEmpty()) {
                        articleDao.insertArticles(parsed);
                        totalInserted += parsed.size();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching feed: " + feed.getUrl(), e);
                    error = e.getMessage();
                }
            }

            // Reload database
            loadArticles(currentUserId, currentCategory);
            loadBookmarkedArticles(currentUserId);
            isSyncingLiveData.postValue(false);

            if (callback != null) {
                callback.onComplete(totalInserted, error);
            }
        });
    }

    public void toggleBookmark(long userId, Article article, Runnable onComplete) {
        executor.execute(() -> {
            if (userId <= 0 || article == null) return;
            boolean currentlyBookmarked = bookmarkDao.isBookmarked(userId, article.getId());
            if (currentlyBookmarked) {
                bookmarkDao.removeBookmark(userId, article.getId());
                article.setBookmarked(false);
            } else {
                bookmarkDao.addBookmark(userId, article.getId());
                article.setBookmarked(true);
            }
            loadBookmarkedArticles(userId);
            if (onComplete != null) onComplete.run();
        });
    }

    public void markAsRead(long articleId) {
        executor.execute(() -> articleDao.markAsRead(articleId));
    }

    public Article getArticleById(long articleId, long currentUserId) {
        return articleDao.getArticleById(articleId, currentUserId);
    }
}
