package com.example.rssreader.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.rssreader.R;
import com.example.rssreader.data.model.Article;
import com.example.rssreader.databinding.FragmentSavedBinding;
import com.example.rssreader.ui.home.ArticleAdapter;

public class SavedFragment extends Fragment implements ArticleAdapter.OnArticleClickListener {

    private FragmentSavedBinding binding;
    private SavedViewModel viewModel;
    private ArticleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SavedViewModel.class);

        setupRecyclerView();
        setupGuestView();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStateAndLoad();
    }

    private void checkLoginStateAndLoad() {
        if (!viewModel.isLoggedIn()) {
            binding.layoutGuest.setVisibility(View.VISIBLE);
            binding.rvSavedArticles.setVisibility(View.GONE);
            binding.tvEmptySaved.setVisibility(View.GONE);
        } else {
            binding.layoutGuest.setVisibility(View.GONE);
            binding.rvSavedArticles.setVisibility(View.VISIBLE);
            viewModel.loadSavedArticles();
        }
    }

    private void setupRecyclerView() {
        adapter = new ArticleAdapter(this);
        binding.rvSavedArticles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSavedArticles.setAdapter(adapter);
    }

    private void setupGuestView() {
        binding.btnLoginFromSaved.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_saved_to_login);
        });
    }

    private void observeViewModel() {
        viewModel.getSavedArticles().observe(getViewLifecycleOwner(), articles -> {
            if (viewModel.isLoggedIn()) {
                adapter.submitList(articles);
                binding.tvEmptySaved.setVisibility((articles == null || articles.isEmpty()) ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onArticleClick(Article article) {
        Bundle args = new Bundle();
        args.putSerializable("article", article);
        args.putLong("article_id", article.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_saved_to_detail, args);
    }

    @Override
    public void onBookmarkClick(Article article) {
        viewModel.toggleBookmark(article);
        Toast.makeText(requireContext(), "Đã bỏ lưu bài viết", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
