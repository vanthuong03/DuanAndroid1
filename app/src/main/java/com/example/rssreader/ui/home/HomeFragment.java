package com.example.rssreader.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.rssreader.R;
import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.model.Feed;
import com.example.rssreader.data.model.FilterState;
import com.example.rssreader.databinding.FragmentHomeBinding;
import com.google.android.material.chip.Chip;

import java.util.List;

public class HomeFragment extends Fragment implements ArticleAdapter.OnArticleClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ArticleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupSortButton();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadArticles();
    }

    private void setupRecyclerView() {
        adapter = new ArticleAdapter(this);
        binding.rvArticles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvArticles.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshFeeds((count, error) -> {
                requireActivity().runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    if (error != null) {
                        Toast.makeText(requireContext(), "Làm mới tin: " + error, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Đã cập nhật bài viết mới", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSortButton() {
        binding.btnSort.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_sort, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.sort_newest) {
                    viewModel.setSortOrder(FilterState.SortOrder.NEWEST);
                    Toast.makeText(requireContext(), "Sắp xếp: Mới nhất trước", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.sort_oldest) {
                    viewModel.setSortOrder(FilterState.SortOrder.OLDEST);
                    Toast.makeText(requireContext(), "Sắp xếp: Cũ nhất trước", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.sort_feed_a_z) {
                    viewModel.setSortOrder(FilterState.SortOrder.FEED_A_TO_Z);
                    Toast.makeText(requireContext(), "Sắp xếp: Theo nguồn (A - Z)", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void observeViewModel() {
        viewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            adapter.submitList(articles);
            binding.tvEmpty.setVisibility((articles == null || articles.isEmpty()) ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsSyncing().observe(getViewLifecycleOwner(), isSyncing -> {
            if (!binding.swipeRefresh.isRefreshing()) {
                binding.progressBar.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getFeeds().observe(getViewLifecycleOwner(), this::setupFilterChips);
    }

    private void setupFilterChips(List<Feed> feeds) {
        if (binding == null) return;
        binding.chipGroupCategories.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        FilterState currentState = viewModel.getFilterState().getValue();
        if (currentState == null) currentState = new FilterState();

        // 1. Chip "Tất cả"
        Chip allChip = (Chip) inflater.inflate(R.layout.item_chip, binding.chipGroupCategories, false);
        allChip.setText(R.string.category_all);
        allChip.setChecked(currentState.getFilterType() == FilterState.FilterType.ALL);
        allChip.setOnClickListener(v -> viewModel.setFilterType(FilterState.FilterType.ALL, -1));
        binding.chipGroupCategories.addView(allChip);

        // 2. Chip "Chưa đọc"
        Chip unreadChip = (Chip) inflater.inflate(R.layout.item_chip, binding.chipGroupCategories, false);
        unreadChip.setText("Chưa đọc");
        unreadChip.setChecked(currentState.getFilterType() == FilterState.FilterType.UNREAD);
        unreadChip.setOnClickListener(v -> viewModel.setFilterType(FilterState.FilterType.UNREAD, -1));
        binding.chipGroupCategories.addView(unreadChip);

        // 3. Chip cho từng nguồn đang bật
        if (feeds != null) {
            for (Feed feed : feeds) {
                if (!feed.isEnabled()) continue;
                Chip feedChip = (Chip) inflater.inflate(R.layout.item_chip, binding.chipGroupCategories, false);
                feedChip.setText(feed.getTitle());
                boolean isThisFeedSelected = (currentState.getFilterType() == FilterState.FilterType.BY_FEED &&
                        currentState.getSelectedFeedId() == feed.getId());
                feedChip.setChecked(isThisFeedSelected);
                feedChip.setOnClickListener(v -> viewModel.setFilterType(FilterState.FilterType.BY_FEED, feed.getId()));
                binding.chipGroupCategories.addView(feedChip);
            }
        }
    }

    @Override
    public void onArticleClick(Article article) {
        Bundle args = new Bundle();
        args.putSerializable("article", article);
        args.putLong("article_id", article.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_home_to_detail, args);
    }

    @Override
    public void onBookmarkClick(Article article) {
        if (!viewModel.isLoggedIn()) {
            Toast.makeText(requireContext(), R.string.guest_mode_notice, Toast.LENGTH_LONG).show();
            Navigation.findNavController(requireView()).navigate(R.id.action_home_to_login);
            return;
        }

        viewModel.toggleBookmark(article, () -> {
            requireActivity().runOnUiThread(() -> {
                String msg = article.isBookmarked() ? "Đã lưu bài viết" : "Đã bỏ lưu bài viết";
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
