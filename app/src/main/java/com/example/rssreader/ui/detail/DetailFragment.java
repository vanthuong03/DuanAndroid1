package com.example.rssreader.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.rssreader.R;
import com.example.rssreader.data.model.Article;
import com.example.rssreader.data.model.ReaderStyle;
import com.example.rssreader.databinding.DialogReaderStyleBinding;
import com.example.rssreader.databinding.FragmentDetailBinding;
import com.example.rssreader.util.DateUtils;

import java.util.Locale;

public class DetailFragment extends Fragment implements TextToSpeech.OnInitListener {

    private FragmentDetailBinding binding;
    private DetailViewModel viewModel;
    private Article currentArticle;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        tts = new TextToSpeech(requireContext(), this);

        setupToolbar();
        setupReaderToggleButtons();

        if (getArguments() != null) {
            Article article = (Article) getArguments().getSerializable("article");
            long articleId = getArguments().getLong("article_id", -1);
            if (article != null) {
                viewModel.setArticle(article);
            } else if (articleId > 0) {
                viewModel.loadArticle(articleId);
            }
        }

        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private void setupReaderToggleButtons() {
        binding.btnToggleReader.setOnClickListener(v -> enableReaderMode());
        binding.btnReaderBackToSummary.setOnClickListener(v -> viewModel.setReaderModeActive(false));
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareArticle();
            return true;
        } else if (id == R.id.action_bookmark) {
            handleBookmark();
            return true;
        } else if (id == R.id.action_speak) {
            speakArticle();
            return true;
        } else if (id == R.id.action_reader_mode) {
            Boolean isActive = viewModel.getIsReaderModeActive().getValue();
            if (Boolean.TRUE.equals(isActive)) {
                viewModel.setReaderModeActive(false);
            } else {
                enableReaderMode();
            }
            return true;
        } else if (id == R.id.action_reader_style) {
            showReaderStyleDialog();
            return true;
        }
        return false;
    }

    private void enableReaderMode() {
        viewModel.setReaderModeActive(true);
        viewModel.loadReaderContent(new DetailViewModel.ContentCallback() {
            @Override
            public void onSuccess(String content) {}

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    // If error and no content, fallback to overview mode
                    if (currentArticle != null && (currentArticle.getContent() == null || currentArticle.getContent().isEmpty())) {
                        viewModel.setReaderModeActive(false);
                    }
                });
            }
        });
    }

    private void showReaderStyleDialog() {
        DialogReaderStyleBinding dialogBinding = DialogReaderStyleBinding.inflate(getLayoutInflater());
        ReaderStyle currentStyle = viewModel.getReaderStyle().getValue();
        if (currentStyle == null) currentStyle = new ReaderStyle();

        // Check font size
        switch (currentStyle.getFontSize()) {
            case ReaderStyle.FONT_SIZE_SMALL: dialogBinding.rbReaderFontSmall.setChecked(true); break;
            case ReaderStyle.FONT_SIZE_LARGE: dialogBinding.rbReaderFontLarge.setChecked(true); break;
            case ReaderStyle.FONT_SIZE_XLARGE: dialogBinding.rbReaderFontXLarge.setChecked(true); break;
            case ReaderStyle.FONT_SIZE_MEDIUM:
            default: dialogBinding.rbReaderFontMedium.setChecked(true); break;
        }

        // Check theme
        switch (currentStyle.getTheme()) {
            case ReaderStyle.THEME_DARK: dialogBinding.rbThemeDark.setChecked(true); break;
            case ReaderStyle.THEME_SEPIA: dialogBinding.rbThemeSepia.setChecked(true); break;
            case ReaderStyle.THEME_LIGHT:
            default: dialogBinding.rbThemeLight.setChecked(true); break;
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    int selectedSize = ReaderStyle.FONT_SIZE_MEDIUM;
                    int checkedSizeId = dialogBinding.rgReaderFontSize.getCheckedRadioButtonId();
                    if (checkedSizeId == R.id.rbReaderFontSmall) selectedSize = ReaderStyle.FONT_SIZE_SMALL;
                    else if (checkedSizeId == R.id.rbReaderFontLarge) selectedSize = ReaderStyle.FONT_SIZE_LARGE;
                    else if (checkedSizeId == R.id.rbReaderFontXLarge) selectedSize = ReaderStyle.FONT_SIZE_XLARGE;

                    int selectedTheme = ReaderStyle.THEME_LIGHT;
                    int checkedThemeId = dialogBinding.rgReaderTheme.getCheckedRadioButtonId();
                    if (checkedThemeId == R.id.rbThemeDark) selectedTheme = ReaderStyle.THEME_DARK;
                    else if (checkedThemeId == R.id.rbThemeSepia) selectedTheme = ReaderStyle.THEME_SEPIA;

                    viewModel.updateReaderStyle(selectedSize, selectedTheme);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getArticle().observe(getViewLifecycleOwner(), article -> {
            if (article == null) return;
            this.currentArticle = article;
            displayArticle(article);
            updateBookmarkMenuIcon(article.isBookmarked());
        });

        viewModel.getIsReaderLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarReader.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsReaderModeActive().observe(getViewLifecycleOwner(), isActive -> {
            if (Boolean.TRUE.equals(isActive)) {
                binding.layoutOverview.setVisibility(View.GONE);
                binding.layoutReaderMode.setVisibility(View.VISIBLE);
                binding.toolbar.setTitle("Chế độ đọc");
            } else {
                binding.layoutOverview.setVisibility(View.VISIBLE);
                binding.layoutReaderMode.setVisibility(View.GONE);
                binding.toolbar.setTitle("Chi tiết bài viết");
                resetOverviewColors();
            }
        });

        viewModel.getReaderContent().observe(getViewLifecycleOwner(), content -> {
            if (content != null && !content.isEmpty()) {
                binding.tvReaderContent.setText(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT));
            }
        });

        viewModel.getReaderStyle().observe(getViewLifecycleOwner(), this::applyReaderStyle);
    }

    private void displayArticle(Article article) {
        // Overview mode values
        binding.tvDetailTitle.setText(article.getTitle());
        binding.tvDetailDescription.setText(article.getDescription());
        binding.tvPubDate.setText(DateUtils.formatRelativeTime(article.getPubDate()));

        if (article.getFeedTitle() != null && !article.getFeedTitle().isEmpty()) {
            binding.tvFeedSource.setText(article.getFeedTitle());
            binding.tvFeedSource.setVisibility(View.VISIBLE);
        } else {
            binding.tvFeedSource.setVisibility(View.GONE);
        }

        // Reader mode values
        binding.tvReaderTitle.setText(article.getTitle());
        String sourceDate = (article.getFeedTitle() != null ? article.getFeedTitle() + " • " : "") +
                DateUtils.formatRelativeTime(article.getPubDate());
        binding.tvReaderSource.setText(sourceDate);

        // Thumbnail
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            binding.ivDetailImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(binding.ivDetailImage);
        } else {
            binding.ivDetailImage.setVisibility(View.GONE);
        }

        binding.btnOpenWeb.setOnClickListener(v -> openOriginalLink());
    }

    private void applyReaderStyle(ReaderStyle style) {
        if (style == null) return;

        // Apply font size
        binding.tvReaderContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.getFontSizeInSp());

        // Apply theme colors when in Reader mode
        Boolean isReaderActive = viewModel.getIsReaderModeActive().getValue();
        if (Boolean.TRUE.equals(isReaderActive)) {
            binding.scrollViewDetail.setBackgroundColor(style.getBackgroundColor());
            binding.layoutReaderMode.setBackgroundColor(style.getBackgroundColor());
            binding.tvReaderTitle.setTextColor(style.getTextColor());
            binding.tvReaderContent.setTextColor(style.getTextColor());
            binding.tvReaderSource.setTextColor(style.getSecondaryTextColor());
        }
    }

    private void resetOverviewColors() {
        int defaultBg = ContextCompat.getColor(requireContext(), android.R.color.transparent);
        binding.scrollViewDetail.setBackgroundColor(defaultBg);
    }

    private void openOriginalLink() {
        if (currentArticle != null && currentArticle.getLink() != null && !currentArticle.getLink().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentArticle.getLink()));
            startActivity(browserIntent);
        }
    }

    private void handleBookmark() {
        if (!viewModel.isLoggedIn()) {
            Toast.makeText(requireContext(), R.string.guest_mode_notice, Toast.LENGTH_LONG).show();
            Navigation.findNavController(requireView()).navigate(R.id.action_detail_to_login);
            return;
        }

        viewModel.toggleBookmark(() -> {
            requireActivity().runOnUiThread(() -> {
                boolean isSaved = currentArticle != null && currentArticle.isBookmarked();
                updateBookmarkMenuIcon(isSaved);
                Toast.makeText(requireContext(), isSaved ? "Đã lưu bài viết" : "Đã bỏ lưu", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateBookmarkMenuIcon(boolean isBookmarked) {
        MenuItem item = binding.toolbar.getMenu().findItem(R.id.action_bookmark);
        if (item != null) {
            item.setIcon(isBookmarked ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
            if (isBookmarked) {
                item.getIcon().setTint(ContextCompat.getColor(requireContext(), R.color.bookmark_gold));
            } else {
                item.getIcon().setTint(ContextCompat.getColor(requireContext(), R.color.on_surface));
            }
        }
    }

    private void shareArticle() {
        if (currentArticle == null || currentArticle.getLink() == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentArticle.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentArticle.getTitle() + "\n" + currentArticle.getLink());
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
    }

    private void speakArticle() {
        if (!isTtsReady || currentArticle == null) {
            Toast.makeText(requireContext(), "Bộ đọc giọng nói chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tts.isSpeaking()) {
            tts.stop();
            Toast.makeText(requireContext(), "Đã dừng đọc", Toast.LENGTH_SHORT).show();
            return;
        }

        String textToSpeak = currentArticle.getTitle() + ". " + (currentArticle.getDescription() != null ? currentArticle.getDescription() : "");
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "rss_article_tts");
        Toast.makeText(requireContext(), "Đang đọc bài viết...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale vn = new Locale("vi", "VN");
            int result = tts.setLanguage(vn);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.getDefault());
            }
            isTtsReady = true;
        }
    }

    @Override
    public void onDestroyView() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroyView();
        binding = null;
    }
}
