package com.example.rssreader.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rssreader.data.model.User;
import com.example.rssreader.data.repository.UserRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> accountDeleted = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        loadUser();
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getAccountDeleted() {
        return accountDeleted;
    }

    public void clearMessages() {
        statusMessage.setValue(null);
        errorMessage.setValue(null);
    }

    public void loadUser() {
        long currentUserId = userRepository.getCurrentUserId();
        if (currentUserId > 0) {
            userRepository.getUserById(currentUserId, userLiveData::postValue);
        }
    }

    public void updateFullName(String newFullName) {
        clearMessages();
        long currentUserId = userRepository.getCurrentUserId();
        userRepository.updateFullName(currentUserId, newFullName, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                statusMessage.postValue(message);
                loadUser();
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    public void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        clearMessages();
        long currentUserId = userRepository.getCurrentUserId();
        userRepository.changePassword(currentUserId, oldPassword, newPassword, confirmPassword, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                statusMessage.postValue(message);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    public void deleteAccount(String password) {
        clearMessages();
        long currentUserId = userRepository.getCurrentUserId();
        userRepository.deleteAccount(currentUserId, password, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                statusMessage.postValue(message);
                accountDeleted.postValue(true);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    public void logout() {
        userRepository.logout();
    }
}
