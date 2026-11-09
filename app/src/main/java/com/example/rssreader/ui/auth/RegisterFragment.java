package com.example.rssreader.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rssreader.R;
import com.example.rssreader.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupCloseButtonAndBack();
        setupActions();
        observeViewModel();
    }

    private void setupCloseButtonAndBack() {
        // Khi chuyển từ Đăng nhập sang Đăng ký rồi bấm X, vẫn về thẳng Trang chủ theo mục 6.3
        binding.btnCloseRegister.setOnClickListener(v -> returnToHome());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToHome();
            }
        });
    }

    private void returnToHome() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack(R.id.navigation_home, false);
    }

    private void setupActions() {
        binding.btnRegister.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
            String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

            binding.tvError.setVisibility(View.GONE);
            viewModel.register(fullName, email, password, confirmPassword);
        });

        binding.tvGoToLogin.setOnClickListener(v -> {
            viewModel.clearState();
            Navigation.findNavController(requireView()).navigate(R.id.action_register_to_login);
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.tvError.setText(error);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });

        viewModel.getAuthSuccess().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Toast.makeText(requireContext(), "Tạo tài khoản thành công! Chào mừng " + user.getFullName(), Toast.LENGTH_SHORT).show();
                NavController navController = NavHostFragment.findNavController(this);
                // Đăng ký xong tự động đăng nhập và quay về Trang chủ
                navController.popBackStack(R.id.navigation_home, false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
