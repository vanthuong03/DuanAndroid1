package com.example.rssreader.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.rssreader.R;
import com.example.rssreader.data.local.ArticleDao;
import com.example.rssreader.data.local.DatabaseHelper;
import com.example.rssreader.data.local.FeedDao;
import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.model.Feed;
import com.example.rssreader.data.remote.RssFetcher;
import com.example.rssreader.data.remote.RssParser;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RefreshWorker extends Worker {
    private static final String TAG = "RefreshWorker";
    private static final String WORK_NAME = "rss_periodic_refresh";
    private static final String CHANNEL_ID = "rss_news_channel";

    public RefreshWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        FeedDao feedDao = new FeedDao(helper);
        ArticleDao articleDao = new ArticleDao(helper);
        RssFetcher fetcher = new RssFetcher();
        RssParser parser = new RssParser();

        List<Feed> feeds = feedDao.getEnabledFeeds();
        int totalNew = 0;

        for (Feed feed : feeds) {
            try {
                String xml = fetcher.fetchXml(feed.getUrl());
                List<Article> articles = parser.parse(xml, feed.getId());
                if (!articles.isEmpty()) {
                    articleDao.insertArticles(articles);
                    totalNew += articles.size();
                }
            } catch (Exception e) {
                Log.e(TAG, "Sync failed for " + feed.getUrl(), e);
            }
        }

        if (totalNew > 0) {
            sendNotification(context, "RSS Reader", "Đã cập nhật bài viết mới!");
        }

        return Result.success();
    }

    private void sendNotification(Context context, String title, String message) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo tin tức",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_rss)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        nm.notify(1001, builder.build());
    }

    public static void schedulePeriodicWork(Context context, int intervalMinutes) {
        if (intervalMinutes < 15) {
            intervalMinutes = 15; // Minimum periodic interval in WorkManager is 15 minutes
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                RefreshWorker.class,
                intervalMinutes,
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }
}
