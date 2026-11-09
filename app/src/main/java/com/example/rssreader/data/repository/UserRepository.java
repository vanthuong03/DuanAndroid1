package com.example.rssreader.data.repository;

import android.content.Context;

import com.example.rssreader.data.local.DatabaseHelper;
import com.example.rssreader.data.local.UserDao;
import com.example.rssreader.data.model.User;
import com.example.rssreader.util.PasswordUtils;
import com.example.rssreader.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess(String message);
        void onError(String message);
    }

    public interface UserCallback {
        void onLoaded(User user);
    }

    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public UserRepository(Context context) {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        this.userDao = new UserDao(helper);
        this.sessionManager = new SessionManager(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public void getUserById(long userId, UserCallback callback) {
        executor.execute(() -> {
            User user = userDao.findById(userId);
            if (callback != null) {
                callback.onLoaded(user);
            }
        });
    }

    public void register(String fullName, String email, String password, String confirmPassword, AuthCallback callback) {
        executor.execute(() -> {
            if (fullName == null || fullName.trim().isEmpty()) {
                callback.onError("Họ và tên không được để trống");
                return;
            }
            if (email == null || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                callback.onError("Email không đúng định dạng");
                return;
            }
            if (password == null || password.length() < 6) {
                callback.onError("Mật khẩu phải có tối thiểu 6 ký tự");
                return;
            }
            if (!password.equals(confirmPassword)) {
                callback.onError("Mật khẩu xác nhận không trùng khớp");
                return;
            }

            String cleanEmail = email.trim().toLowerCase();
            if (userDao.isEmailTaken(cleanEmail)) {
                callback.onError("Email này đã được đăng ký");
                return;
            }

            String salt = PasswordUtils.generateSalt();
            String hash = PasswordUtils.hashPassword(password, salt);
            long createdAt = System.currentTimeMillis();

            User user = new User(fullName.trim(), cleanEmail, hash, salt, createdAt);
            long newId = userDao.insert(user);
            if (newId > 0) {
                user.setId(newId);
                sessionManager.saveUserSession(newId, user.getEmail(), user.getFullName());
                callback.onSuccess(user);
            } else {
                callback.onError("Không thể tạo tài khoản. Vui lòng thử lại!");
            }
        });
    }

    public void login(String email, String password, AuthCallback callback) {
        executor.execute(() -> {
            if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
                callback.onError("Email hoặc mật khẩu không đúng");
                return;
            }

            String cleanEmail = email.trim().toLowerCase();
            User user = userDao.findByEmail(cleanEmail);
            if (user == null) {
                callback.onError("Email hoặc mật khẩu không đúng");
                return;
            }

            boolean isMatch = PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash());
            if (!isMatch) {
                callback.onError("Email hoặc mật khẩu không đúng");
                return;
            }

            sessionManager.saveUserSession(user.getId(), user.getEmail(), user.getFullName());
            callback.onSuccess(user);
        });
    }

    public void updateFullName(long userId, String newFullName, SimpleCallback callback) {
        executor.execute(() -> {
            if (newFullName == null || newFullName.trim().isEmpty()) {
                callback.onError("Họ và tên không được để trống");
                return;
            }
            User user = userDao.findById(userId);
            if (user == null) {
                callback.onError("Tài khoản không tồn tại");
                return;
            }
            int updated = userDao.updateFullName(userId, newFullName.trim());
            if (updated > 0) {
                sessionManager.saveUserSession(userId, user.getEmail(), newFullName.trim());
                callback.onSuccess("Cập nhật họ tên thành công");
            } else {
                callback.onError("Không thể cập nhật họ tên");
            }
        });
    }

    public void changePassword(long userId, String oldPassword, String newPassword, String confirmPassword, SimpleCallback callback) {
        executor.execute(() -> {
            if (oldPassword == null || oldPassword.isEmpty()) {
                callback.onError("Vui lòng nhập mật khẩu hiện tại");
                return;
            }
            if (newPassword == null || newPassword.length() < 6) {
                callback.onError("Mật khẩu mới phải có tối thiểu 6 ký tự");
                return;
            }
            if (oldPassword.equals(newPassword)) {
                callback.onError("Mật khẩu mới không được trùng với mật khẩu cũ");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                callback.onError("Mật khẩu xác nhận không trùng khớp");
                return;
            }

            User user = userDao.findById(userId);
            if (user == null) {
                callback.onError("Tài khoản không tồn tại");
                return;
            }

            boolean isOldPasswordCorrect = PasswordUtils.verifyPassword(oldPassword, user.getSalt(), user.getPasswordHash());
            if (!isOldPasswordCorrect) {
                callback.onError("Mật khẩu hiện tại không chính xác");
                return;
            }

            String newSalt = PasswordUtils.generateSalt();
            String newHash = PasswordUtils.hashPassword(newPassword, newSalt);

            int updated = userDao.updatePassword(userId, newHash, newSalt);
            if (updated > 0) {
                callback.onSuccess("Đổi mật khẩu thành công");
            } else {
                callback.onError("Đổi mật khẩu thất bại. Vui lòng thử lại!");
            }
        });
    }

    public void deleteAccount(long userId, String password, SimpleCallback callback) {
        executor.execute(() -> {
            if (password == null || password.isEmpty()) {
                callback.onError("Vui lòng nhập mật khẩu xác nhận");
                return;
            }
            User user = userDao.findById(userId);
            if (user == null) {
                callback.onError("Tài khoản không tồn tại");
                return;
            }

            boolean isPasswordCorrect = PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash());
            if (!isPasswordCorrect) {
                callback.onError("Mật khẩu xác nhận không chính xác");
                return;
            }

            int deleted = userDao.deleteUser(userId);
            if (deleted > 0) {
                sessionManager.clearSession();
                callback.onSuccess("Đã xóa tài khoản thành công");
            } else {
                callback.onError("Xóa tài khoản thất bại");
            }
        });
    }

    public void logout() {
        sessionManager.clearSession();
    }
}
