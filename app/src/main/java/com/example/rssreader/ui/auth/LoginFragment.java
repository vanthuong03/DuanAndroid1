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
import com.example.rssreader.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
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
        // Nút X ở góc trên bên phải đóng màn hình và quay về Trang chủ ở chế độ khách
        binding.btnCloseLogin.setOnClickListener(v -> returnToHome());

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
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            binding.tvError.setVisibility(View.GONE);
            viewModel.login(email, password);
        });

        binding.tvGoToRegister.setOnClickListener(v -> {
            viewModel.clearState();
            Navigation.findNavController(requireView()).navigate(R.id.action_login_to_register);
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
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
                Toast.makeText(requireContext(), "Đăng nhập thành công! Chào " + user.getFullName(), Toast.LENGTH_SHORT).show();
                NavController navController = NavHostFragment.findNavController(this);
                // Quay lại màn hình trước đó
                if (!navController.popBackStack()) {
                    navController.navigate(R.id.navigation_home);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
