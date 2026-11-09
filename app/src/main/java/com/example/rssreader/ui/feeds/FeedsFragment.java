package com.example.rssreader.ui.feeds;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.rssreader.data.model.Feed;
import com.example.rssreader.databinding.DialogAddFeedBinding;
import com.example.rssreader.databinding.FragmentFeedsBinding;

public class FeedsFragment extends Fragment implements FeedAdapter.OnFeedInteractionListener {

    private FragmentFeedsBinding binding;
    private FeedsViewModel viewModel;
    private FeedAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFeedsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FeedsViewModel.class);

        setupRecyclerView();
        setupFab();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new FeedAdapter(this);
        binding.rvFeeds.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFeeds.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabAddFeed.setOnClickListener(v -> showAddFeedDialog());
    }

    private void observeViewModel() {
        viewModel.getFeeds().observe(getViewLifecycleOwner(), feeds -> {
            adapter.submitList(feeds);
            binding.tvEmptyFeeds.setVisibility((feeds == null || feeds.isEmpty()) ? View.VISIBLE : View.GONE);
        });
    }

    private void showAddFeedDialog() {
        DialogAddFeedBinding dialogBinding = DialogAddFeedBinding.inflate(getLayoutInflater());

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String title = dialogBinding.etFeedTitle.getText() != null ? dialogBinding.etFeedTitle.getText().toString().trim() : "";
                    String url = dialogBinding.etFeedUrl.getText() != null ? dialogBinding.etFeedUrl.getText().toString().trim() : "";
                    String category = dialogBinding.etFeedCategory.getText() != null ? dialogBinding.etFeedCategory.getText().toString().trim() : "";

                    if (url.isEmpty()) {
                        Toast.makeText(requireContext(), "URL không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (title.isEmpty()) {
                        title = url;
                    }

                    viewModel.addFeed(title, url, category, () -> {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Đã thêm nguồn tin", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onToggleEnable(Feed feed, boolean isEnabled) {
        viewModel.setFeedEnabled(feed.getId(), isEnabled);
    }

    @Override
    public void onDelete(Feed feed) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa nguồn tin")
                .setMessage("Bạn có chắc chắn muốn xóa nguồn \"" + feed.getTitle() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteFeed(feed.getId());
                    Toast.makeText(requireContext(), "Đã xóa nguồn tin", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
