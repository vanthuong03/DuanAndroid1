package com.example.rssreader.ui.saved;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.repository.ArticleRepository;
import com.example.rssreader.util.SessionManager;

import java.util.List;

public class SavedViewModel extends AndroidViewModel {

    private final ArticleRepository articleRepository;
    private final SessionManager sessionManager;

    public SavedViewModel(@NonNull Application application) {
        super(application);
        this.articleRepository = new ArticleRepository(application);
        this.sessionManager = new SessionManager(application);
    }

    public LiveData<List<Article>> getSavedArticles() {
        return articleRepository.getBookmarkedArticlesLiveData();
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void loadSavedArticles() {
        if (sessionManager.isLoggedIn()) {
            articleRepository.loadBookmarkedArticles(sessionManager.getUserId());
        }
    }

    public void toggleBookmark(Article article) {
        if (!sessionManager.isLoggedIn()) return;
        articleRepository.toggleBookmark(sessionManager.getUserId(), article, () -> {
            loadSavedArticles();
        });
    }
}
