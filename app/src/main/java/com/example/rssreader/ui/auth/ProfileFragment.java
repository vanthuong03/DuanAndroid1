package com.example.rssreader.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.rssreader.R;
import com.example.rssreader.data.model.User;
import com.example.rssreader.databinding.DialogChangePasswordBinding;
import com.example.rssreader.databinding.DialogDeleteAccountBinding;
import com.example.rssreader.databinding.FragmentProfileBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupCloseButtonAndBack();
        setupActions();
        observeViewModel();
    }

    private void setupCloseButtonAndBack() {
        // Nút X ở góc trên bên phải quay về Trang chủ
        binding.btnCloseProfile.setOnClickListener(v -> returnToHome());

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
        binding.btnSaveName.setOnClickListener(v -> {
            String newName = binding.etProfileFullName.getText() != null ? binding.etProfileFullName.getText().toString().trim() : "";
            viewModel.updateFullName(newName);
        });

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.btnGoToHistory.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_history);
        });

        binding.btnProfileLogout.setOnClickListener(v -> {
            viewModel.logout();
            Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            returnToHome();
        });

        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showChangePasswordDialog() {
        DialogChangePasswordBinding dialogBinding = DialogChangePasswordBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Đổi mật khẩu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String oldPass = dialogBinding.etOldPassword.getText() != null ? dialogBinding.etOldPassword.getText().toString() : "";
                String newPass = dialogBinding.etNewPassword.getText() != null ? dialogBinding.etNewPassword.getText().toString() : "";
                String confirmPass = dialogBinding.etConfirmNewPassword.getText() != null ? dialogBinding.etConfirmNewPassword.getText().toString() : "";

                viewModel.changePassword(oldPass, newPass, confirmPass);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showDeleteAccountDialog() {
        DialogDeleteAccountBinding dialogBinding = DialogDeleteAccountBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Xác nhận xóa", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String password = dialogBinding.etDeletePassword.getText() != null ? dialogBinding.etDeletePassword.getText().toString() : "";
                viewModel.deleteAccount(password);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void observeViewModel() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                displayUser(user);
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getAccountDeleted().observe(getViewLifecycleOwner(), isDeleted -> {
            if (Boolean.TRUE.equals(isDeleted)) {
                returnToHome();
            }
        });
    }

    private void displayUser(User user) {
        binding.etProfileFullName.setText(user.getFullName());
        binding.tvProfileEmail.setText(user.getEmail());

        if (user.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.tvProfileCreatedAt.setText(sdf.format(new Date(user.getCreatedAt())));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
