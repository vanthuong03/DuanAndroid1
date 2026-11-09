package com.example.rssreader.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rssreader.data.model.User;
import com.example.rssreader.data.repository.UserRepository;

public class AuthViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<User> authSuccess = new MutableLiveData<>(null);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<User> getAuthSuccess() {
        return authSuccess;
    }

    public void clearState() {
        errorMessage.setValue(null);
        authSuccess.setValue(null);
        isLoading.setValue(false);
    }

    public void register(String fullName, String email, String password, String confirmPassword) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        userRepository.register(fullName, email, password, confirmPassword, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                isLoading.postValue(false);
                authSuccess.postValue(user);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        userRepository.login(email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                isLoading.postValue(false);
                authSuccess.postValue(user);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void logout() {
        userRepository.logout();
    }
}
