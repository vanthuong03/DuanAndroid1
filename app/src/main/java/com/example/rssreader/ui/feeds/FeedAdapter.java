package com.example.rssreader.ui.feeds;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rssreader.data.model.Feed;
import com.example.rssreader.databinding.ItemFeedBinding;

public class FeedAdapter extends ListAdapter<Feed, FeedAdapter.FeedViewHolder> {

    public interface OnFeedInteractionListener {
        void onToggleEnable(Feed feed, boolean isEnabled);
        void onDelete(Feed feed);
    }

    private final OnFeedInteractionListener listener;

    public FeedAdapter(OnFeedInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Feed> DIFF_CALLBACK = new DiffUtil.ItemCallback<Feed>() {
        @Override
        public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedBinding binding = ItemFeedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ItemFeedBinding binding;

        public FeedViewHolder(ItemFeedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Feed feed) {
            binding.tvFeedTitle.setText(feed.getTitle());
            binding.tvFeedUrl.setText(feed.getUrl());

            if (feed.getCategory() != null && !feed.getCategory().isEmpty()) {
                binding.tvCategoryBadge.setText(feed.getCategory());
                binding.tvCategoryBadge.setVisibility(ViewGroup.VISIBLE);
            } else {
                binding.tvCategoryBadge.setVisibility(ViewGroup.GONE);
            }

            // Remove previous listener to prevent triggering during recycling
            binding.switchEnabled.setOnCheckedChangeListener(null);
            binding.switchEnabled.setChecked(feed.isEnabled());

            binding.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleEnable(feed, isChecked);
                }
            });

            binding.btnDeleteFeed.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(feed);
                }
            });
        }
    }
}
