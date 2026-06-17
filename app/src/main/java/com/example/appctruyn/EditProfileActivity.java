package com.example.appctruyn;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appctruyn.auth.AuthManager;
import com.example.appctruyn.databinding.ActivityEditProfileBinding;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        loadUserData();

        binding.btnUpdateProfile.setOnClickListener(v -> updateProfile());
        binding.btnUpdatePassword.setOnClickListener(v -> updatePassword());
    }

    private void loadUserData() {
        AuthManager.getCurrentUserInfo().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                binding.etDisplayName.setText(task.getResult().getDisplayName());
                binding.etEmail.setText(task.getResult().getEmail());
            }
        });
    }

    private void updateProfile() {
        String name = binding.etDisplayName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.tilDisplayName.setError(getString(R.string.err_empty_name));
            return;
        }
        binding.tilDisplayName.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.err_empty_email));
            return;
        }
        binding.tilEmail.setError(null);

        AuthManager.getCurrentUserInfo().addOnSuccessListener(user -> {
            boolean nameChanged = !name.equals(user.getDisplayName());
            boolean emailChanged = !email.equals(user.getEmail());

            if (!nameChanged && !emailChanged) {
                Toast.makeText(this, "Không có thay đổi nào", Toast.LENGTH_SHORT).show();
                return;
            }

            if (emailChanged) {
                showReauthenticateDialog(password -> {
                    AuthManager.reauthenticate(password).addOnCompleteListener(reAuthTask -> {
                        if (reAuthTask.isSuccessful()) {
                            performProfileUpdate(name, email);
                        } else {
                            Toast.makeText(this, "Xác thực thất bại: " + reAuthTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                performProfileUpdate(name, null);
            }
        });
    }

    private void performProfileUpdate(String name, String email) {
        if (email != null) {
            AuthManager.updateEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AuthManager.updateDisplayName(name).addOnCompleteListener(nameTask -> {
                        Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    Toast.makeText(this, "Lỗi cập nhật email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            AuthManager.updateDisplayName(name).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Cập nhật tên thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi cập nhật tên", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updatePassword() {
        String newPass = binding.etNewPassword.getText().toString();
        String confirmPass = binding.etConfirmNewPassword.getText().toString();

        if (TextUtils.isEmpty(newPass)) {
            binding.tilNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }
        if (newPass.length() < 6) {
            binding.tilNewPassword.setError(getString(R.string.err_password_length));
            return;
        }
        binding.tilNewPassword.setError(null);

        if (!newPass.equals(confirmPass)) {
            binding.tilConfirmNewPassword.setError(getString(R.string.err_password_mismatch));
            return;
        }
        binding.tilConfirmNewPassword.setError(null);

        showReauthenticateDialog(password -> {
            AuthManager.reauthenticate(password).addOnCompleteListener(reAuthTask -> {
                if (reAuthTask.isSuccessful()) {
                    AuthManager.updatePassword(newPass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            binding.etNewPassword.setText("");
                            binding.etConfirmNewPassword.setText("");
                        } else {
                            Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Mật khẩu hiện tại không chính xác", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showReauthenticateDialog(OnPasswordEnteredListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận mật khẩu");
        builder.setMessage("Vui lòng nhập mật khẩu hiện tại để tiếp tục");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_reauthenticate, null);
        final TextInputEditText input = viewInflated.findViewById(R.id.etCurrentPassword);
        builder.setView(viewInflated);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String password = input.getText().toString();
            if (!TextUtils.isEmpty(password)) {
                listener.onPasswordEntered(password);
            } else {
                Toast.makeText(this, "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    interface OnPasswordEnteredListener {
        void onPasswordEntered(String password);
    }
}
