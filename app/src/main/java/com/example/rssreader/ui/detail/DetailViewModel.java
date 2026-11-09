package com.example.rssreader.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.model.ReaderStyle;
import com.example.rssreader.data.remote.ContentExtractor;
import com.example.rssreader.data.repository.ArticleRepository;
import com.example.rssreader.data.repository.HistoryRepository;
import com.example.rssreader.util.PrefsManager;
import com.example.rssreader.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailViewModel extends AndroidViewModel {

    public interface ContentCallback {
        void onSuccess(String content);
        void onError(String message);
    }

    private final ArticleRepository articleRepository;
    private final HistoryRepository historyRepository;
    private final SessionManager sessionManager;
    private final PrefsManager prefsManager;
    private final ContentExtractor contentExtractor;
    private final ExecutorService executor;

    private final MutableLiveData<Article> articleLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> readerContentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isReaderLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isReaderModeActiveLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<ReaderStyle> readerStyleLiveData = new MutableLiveData<>();

    public DetailViewModel(@NonNull Application application) {
        super(application);
        this.articleRepository = new ArticleRepository(application);
        this.historyRepository = new HistoryRepository(application);
        this.sessionManager = new SessionManager(application);
        this.prefsManager = new PrefsManager(application);
        this.contentExtractor = new ContentExtractor();
        this.executor = Executors.newFixedThreadPool(2);

        // Load reader style from preferences
        ReaderStyle style = new ReaderStyle(prefsManager.getReaderFontSize(), prefsManager.getReaderTheme());
        readerStyleLiveData.setValue(style);
    }

    public LiveData<Article> getArticle() {
        return articleLiveData;
    }

    public LiveData<String> getReaderContent() {
        return readerContentLiveData;
    }

    public LiveData<Boolean> getIsReaderLoading() {
        return isReaderLoadingLiveData;
    }

    public LiveData<Boolean> getIsReaderModeActive() {
        return isReaderModeActiveLiveData;
    }

    public LiveData<ReaderStyle> getReaderStyle() {
        return readerStyleLiveData;
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public int getFontSizePreference() {
        return prefsManager.getFontSize();
    }

    public void loadArticle(long articleId) {
        executor.execute(() -> {
            Article article = articleRepository.getArticleById(articleId, sessionManager.getUserId());
            if (article != null) {
                articleRepository.markAsRead(articleId);
                article.setRead(true);
                articleLiveData.postValue(article);

                // Automatically record reading history if user is logged in
                if (sessionManager.isLoggedIn()) {
                    historyRepository.recordReading(sessionManager.getUserId(), articleId);
                }
            }
        });
    }

    public void setArticle(Article article) {
        if (article != null) {
            articleRepository.markAsRead(article.getId());
            article.setRead(true);
            articleLiveData.setValue(article);

            // Automatically record reading history if user is logged in
            if (sessionManager.isLoggedIn()) {
                historyRepository.recordReading(sessionManager.getUserId(), article.getId());
            }
        }
    }

    public void setReaderModeActive(boolean active) {
        isReaderModeActiveLiveData.setValue(active);
    }

    public void loadReaderContent(ContentCallback callback) {
        Article article = articleLiveData.getValue();
        if (article == null) {
            if (callback != null) callback.onError("Bài viết không tồn tại");
            return;
        }

        // Check if cached content is already in SQLite
        if (article.getContent() != null && !article.getContent().trim().isEmpty()) {
            readerContentLiveData.setValue(article.getContent());
            if (callback != null) callback.onSuccess(article.getContent());
            return;
        }

        // Otherwise, fetch from web in background
        isReaderLoadingLiveData.setValue(true);
        executor.execute(() -> {
            try {
                String extracted = contentExtractor.extractArticleContent(article.getLink());
                article.setContent(extracted);
                articleRepository.saveArticleContent(article.getId(), extracted);

                readerContentLiveData.postValue(extracted);
                isReaderLoadingLiveData.postValue(false);
                if (callback != null) callback.onSuccess(extracted);
            } catch (Exception e) {
                isReaderLoadingLiveData.postValue(false);
                if (callback != null) callback.onError("Không thể tải nội dung đầy đủ: " + e.getMessage());
            }
        });
    }

    public void updateReaderStyle(int fontSize, int theme) {
        prefsManager.setReaderFontSize(fontSize);
        prefsManager.setReaderTheme(theme);
        ReaderStyle style = new ReaderStyle(fontSize, theme);
        readerStyleLiveData.setValue(style);
    }

    public void toggleBookmark(Runnable onComplete) {
        Article current = articleLiveData.getValue();
        if (current == null || !sessionManager.isLoggedIn()) return;

        articleRepository.toggleBookmark(sessionManager.getUserId(), current, () -> {
            articleLiveData.postValue(current);
            if (onComplete != null) onComplete.run();
        });
    }
}
