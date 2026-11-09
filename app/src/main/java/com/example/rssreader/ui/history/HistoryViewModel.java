package com.example.rssreader.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.repository.ArticleRepository;
import com.example.rssreader.data.repository.HistoryRepository;
import com.example.rssreader.util.SessionManager;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final HistoryRepository historyRepository;
    private final ArticleRepository articleRepository;
    private final SessionManager sessionManager;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        this.historyRepository = new HistoryRepository(application);
        this.articleRepository = new ArticleRepository(application);
        this.sessionManager = new SessionManager(application);

        loadHistory();
    }

    public LiveData<List<Article>> getHistoryArticles() {
        return historyRepository.getHistoryLiveData();
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void loadHistory() {
        if (sessionManager.isLoggedIn()) {
            historyRepository.loadHistory(sessionManager.getUserId());
        }
    }

    public void deleteItem(Article article) {
        if (!sessionManager.isLoggedIn() || article == null) return;
        historyRepository.deleteHistoryItem(sessionManager.getUserId(), article.getId(), null);
    }

    public void clearAllHistory(Runnable onComplete) {
        if (!sessionManager.isLoggedIn()) return;
        historyRepository.clearHistory(sessionManager.getUserId(), onComplete);
    }

    public void toggleBookmark(Article article, Runnable onComplete) {
        if (!sessionManager.isLoggedIn() || article == null) return;
        articleRepository.toggleBookmark(sessionManager.getUserId(), article, () -> {
            loadHistory();
            if (onComplete != null) onComplete.run();
        });
    }
}
