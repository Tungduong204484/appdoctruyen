package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appctruyn.auth.AuthManager;
import com.example.appctruyn.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AuthManager.isLoggedIn()) {
            goToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (!validate(email, password)) return;

            setLoading(true);
            AuthManager.login(email, password).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    String welcomeMsg = getString(R.string.welcome_user, task.getResult().getDisplayName());
                    Toast.makeText(LoginActivity.this, welcomeMsg, Toast.LENGTH_SHORT).show();
                    goToMain();
                } else {
                    String errorMsg = getString(R.string.error_prefix, friendlyError(task.getException().getMessage()));
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });

        binding.tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.forgot_password_email_hint), Toast.LENGTH_SHORT).show();
                return;
            }
            sendPasswordReset(email);
        });
    }

    private boolean validate(String email, String password) {
        if (email.isEmpty()) {
            binding.etEmail.setError(getString(R.string.err_empty_email));
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
        return true;
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setText(loading ? "" : getString(R.string.login));
    }

    private void sendPasswordReset(String email) {
        FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.reset_email_sent), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    String errorMsg = getString(R.string.err_reset_email_fail, e.getMessage());
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private String friendlyError(String msg) {
        if (msg == null) return getString(R.string.err_unknown);
        String lowerMsg = msg.toLowerCase();
        if (lowerMsg.contains("password")) return getString(R.string.err_wrong_password);
        if (lowerMsg.contains("no user") || lowerMsg.contains("user-not-found")) return getString(R.string.err_user_not_found);
        if (lowerMsg.contains("network")) return getString(R.string.err_network);
        if (lowerMsg.contains("blocked")) return getString(R.string.err_blocked);
        return msg;
    }
}
