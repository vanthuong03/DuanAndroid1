package com.example.rssreader.ui.history;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.rssreader.R;
import com.example.rssreader.data.model.Article;
import com.example.rssreader.databinding.FragmentHistoryBinding;
import com.example.rssreader.ui.home.ArticleAdapter;

public class HistoryFragment extends Fragment implements ArticleAdapter.OnArticleClickListener {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private ArticleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setupToolbar();
        setupRecyclerView();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadHistory();
    }

    private void setupToolbar() {
        binding.toolbarHistory.setNavigationOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
        binding.toolbarHistory.inflateMenu(R.menu.menu_history);
        binding.toolbarHistory.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_history) {
                showClearHistoryDialog();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new ArticleAdapter(this);
        binding.rvHistoryArticles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistoryArticles.setAdapter(adapter);
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa lịch sử")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử đọc bài không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.clearAllHistory(() -> {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Đã xóa lịch sử đọc", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getHistoryArticles().observe(getViewLifecycleOwner(), articles -> {
            adapter.submitList(articles);
            binding.tvEmptyHistory.setVisibility((articles == null || articles.isEmpty()) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onArticleClick(Article article) {
        Bundle args = new Bundle();
        args.putSerializable("article", article);
        args.putLong("article_id", article.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_history_to_detail, args);
    }

    @Override
    public void onBookmarkClick(Article article) {
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
