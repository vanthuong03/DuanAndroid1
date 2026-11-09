package com.example.rssreader.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rssreader.R;
import com.example.rssreader.data.model.Article;
import com.example.rssreader.databinding.ItemArticleBinding;
import com.example.rssreader.util.DateUtils;

public class ArticleAdapter extends ListAdapter<Article, ArticleAdapter.ArticleViewHolder> {

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
        void onBookmarkClick(Article article);
    }

    private final OnArticleClickListener listener;

    public ArticleAdapter(OnArticleClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Article> DIFF_CALLBACK = new DiffUtil.ItemCallback<Article>() {
        @Override
        public boolean areItemsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
            return oldItem.getId() == newItem.getId() ||
                    (oldItem.getLink() != null && oldItem.getLink().equals(newItem.getLink()));
        }

        @Override
        public boolean areContentsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArticleBinding binding = ItemArticleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ArticleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder {
        private final ItemArticleBinding binding;

        public ArticleViewHolder(ItemArticleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Article article) {
            Context context = binding.getRoot().getContext();

            binding.tvTitle.setText(article.getTitle());
            binding.tvDescription.setText(article.getDescription());
            binding.tvDate.setText(DateUtils.formatRelativeTime(article.getPubDate()));

            if (article.getFeedTitle() != null && !article.getFeedTitle().isEmpty()) {
                binding.tvFeedTitle.setText(article.getFeedTitle());
                binding.tvFeedTitle.setVisibility(ViewGroup.VISIBLE);
            } else {
                binding.tvFeedTitle.setVisibility(ViewGroup.GONE);
            }

            // Dim read articles
            if (article.isRead()) {
                binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.article_read_title));
            } else {
                binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.on_surface));
            }

            // Bookmark icon state
            if (article.isBookmarked()) {
                binding.btnBookmark.setImageResource(R.drawable.ic_bookmark);
                binding.btnBookmark.setColorFilter(ContextCompat.getColor(context, R.color.bookmark_gold));
            } else {
                binding.btnBookmark.setImageResource(R.drawable.ic_bookmark_border);
                binding.btnBookmark.setColorFilter(ContextCompat.getColor(context, R.color.on_surface_variant));
            }

            // Thumbnail image with Glide
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(article.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(binding.ivThumbnail);
            } else {
                binding.ivThumbnail.setImageResource(R.drawable.placeholder_image);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onArticleClick(article);
            });

            binding.btnBookmark.setOnClickListener(v -> {
                if (listener != null) listener.onBookmarkClick(article);
            });
        }
    }
}
