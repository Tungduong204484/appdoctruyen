package com.example.appctruyn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appctruyn.auth.AuthManager
import com.example.appctruyn.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthManager.isLoggedIn) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!validate(email, password)) return@setOnClickListener

            setLoading(true)
            lifecycleScope.launch {
                val result = AuthManager.login(email, password)
                setLoading(false)

                result.onSuccess { user ->
                    val welcomeMsg = getString(R.string.welcome_user, user.displayName)
                    Toast.makeText(this@LoginActivity, welcomeMsg, Toast.LENGTH_SHORT).show()
                    goToMain()
                }

                result.onFailure { e ->
                    val errorMsg = getString(R.string.error_prefix, friendlyError(e.message))
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.forgot_password_email_hint), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendPasswordReset(email)
        }
    }

    private fun validate(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.err_empty_email)
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.err_empty_password)
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = getString(R.string.err_password_length)
            return false
        }
        return true
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (loading) "" else getString(R.string.login)
    }

    private fun sendPasswordReset(email: String) {
        com.google.firebase.auth.FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.reset_email_sent), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                val errorMsg = getString(R.string.err_reset_email_fail, it.message)
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun friendlyError(msg: String?): String {
        return when {
            msg == null -> getString(R.string.err_unknown)
            msg.contains("password") -> getString(R.string.err_wrong_password)
            msg.contains("no user") || msg.contains("user-not-found") -> getString(R.string.err_user_not_found)
            msg.contains("network") -> getString(R.string.err_network)
            msg.contains("blocked") -> getString(R.string.err_blocked)
            else -> msg
        }
    }
}
