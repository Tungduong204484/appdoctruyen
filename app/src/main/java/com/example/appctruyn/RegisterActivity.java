package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appctruyn.auth.AuthManager;
import com.example.appctruyn.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        updateRoleUI();
    }

    private void setupListeners() {
        binding.layoutReader.setOnClickListener(v -> {
            binding.rbReader.setChecked(true);
            binding.rbAuthor.setChecked(false);
            updateRoleUI();
        });

        binding.layoutAuthor.setOnClickListener(v -> {
            binding.rbAuthor.setChecked(true);
            binding.rbReader.setChecked(false);
            updateRoleUI();
        });

        binding.rbReader.setOnClickListener(v -> {
            binding.rbAuthor.setChecked(false);
            updateRoleUI();
        });
        binding.rbAuthor.setOnClickListener(v -> {
            binding.rbReader.setChecked(false);
            updateRoleUI();
        });

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirm = binding.etConfirmPassword.getText().toString().trim();

            String role = binding.rbAuthor.isChecked() ? "author" : "reader";

            if (!validate(name, email, password, confirm)) return;

            setLoading(true);
            AuthManager.register(email, password, name, role).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    String successMsg = getString(R.string.register_success, task.getResult().getDisplayName());
                    Toast.makeText(RegisterActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    String errorMsg = getString(R.string.error_prefix, friendlyError(task.getException().getMessage()));
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });

        binding.tvGoLogin.setOnClickListener(v -> finish());
    }

    private void updateRoleUI() {
        boolean isReader = binding.rbReader.isChecked();
        boolean isAuthor = binding.rbAuthor.isChecked();

        binding.layoutReader.setBackgroundResource(
                isReader ? R.drawable.bg_role_card_selected : R.drawable.bg_role_card
        );
        binding.layoutAuthor.setBackgroundResource(
                isAuthor ? R.drawable.bg_role_card_selected : R.drawable.bg_role_card
        );
    }

    private boolean validate(String name, String email, String password, String confirm) {
        if (name.isEmpty()) {
            binding.etName.setError(getString(R.string.err_empty_name));
            return false;
        }
        if (email.isEmpty()) {
            binding.etEmail.setError(getString(R.string.err_empty_email));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError(getString(R.string.err_invalid_email));
            return false;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError(getString(R.string.err_empty_password));
            return false;
        }
        if (password.length() < 6) {
            binding.etPassword.setError(getString(R.string.err_password_length));
            return false;
        }
        if (!password.equals(confirm)) {
            binding.etConfirmPassword.setError(getString(R.string.err_password_mismatch));
            return false;
        }
        return true;
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setText(loading ? "" : getString(R.string.register));
    }

    private String friendlyError(String msg) {
        if (msg == null) return getString(R.string.err_unknown);
        String lowerMsg = msg.toLowerCase();
        if (lowerMsg.contains("email-already-in-use")) return getString(R.string.err_email_in_use);
        if (lowerMsg.contains("weak-password")) return getString(R.string.err_weak_password);
        if (lowerMsg.contains("invalid-email")) return getString(R.string.err_invalid_email);
        if (lowerMsg.contains("network")) return getString(R.string.err_network);
        return msg;
    }
}
