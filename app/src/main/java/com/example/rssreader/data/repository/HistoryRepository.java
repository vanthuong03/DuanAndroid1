package com.example.rssreader.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rssreader.data.local.DatabaseHelper;
import com.example.rssreader.data.local.HistoryDao;
import com.example.rssreader.data.model.Article;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryRepository {

    private final HistoryDao historyDao;
    private final ExecutorService executor;
    private final MutableLiveData<List<Article>> historyLiveData = new MutableLiveData<>();

    public HistoryRepository(Context context) {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        this.historyDao = new HistoryDao(helper);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Article>> getHistoryLiveData() {
        return historyLiveData;
    }

    public void recordReading(long userId, long articleId) {
        if (userId <= 0 || articleId <= 0) return;
        executor.execute(() -> historyDao.recordReading(userId, articleId));
    }

    public void loadHistory(long userId) {
        executor.execute(() -> {
            List<Article> list = historyDao.getReadingHistory(userId);
            historyLiveData.postValue(list);
        });
    }

    public void deleteHistoryItem(long userId, long articleId, Runnable onComplete) {
        executor.execute(() -> {
            historyDao.deleteHistoryItem(userId, articleId);
            loadHistory(userId);
            if (onComplete != null) onComplete.run();
        });
    }

    public void clearHistory(long userId, Runnable onComplete) {
        executor.execute(() -> {
            historyDao.clearHistory(userId);
            loadHistory(userId);
            if (onComplete != null) onComplete.run();
        });
    }
}
