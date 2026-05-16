package com.example.appctruyn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appctruyn.auth.AuthManager
import com.example.appctruyn.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        updateRoleUI()
    }

    private fun setupListeners() {
        binding.layoutReader.setOnClickListener {
            binding.rbReader.isChecked = true
            binding.rbAuthor.isChecked = false
            updateRoleUI()
        }

        binding.layoutAuthor.setOnClickListener {
            binding.rbAuthor.isChecked = true
            binding.rbReader.isChecked = false
            updateRoleUI()
        }

        binding.rbReader.setOnClickListener {
            binding.rbAuthor.isChecked = false
            updateRoleUI()
        }
        binding.rbAuthor.setOnClickListener {
            binding.rbReader.isChecked = false
            updateRoleUI()
        }

        binding.btnRegister.setOnClickListener {
            val name     = binding.etName.text.toString().trim()
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm  = binding.etConfirmPassword.text.toString().trim()

            val role = if (binding.rbAuthor.isChecked) "author" else "reader"

            if (!validate(name, email, password, confirm)) return@setOnClickListener

            setLoading(true)
            lifecycleScope.launch {
                val result = AuthManager.register(email, password, name, role)
                setLoading(false)

                result.onSuccess { user ->
                    val successMsg = getString(R.string.register_success, user.displayName)
                    Toast.makeText(this@RegisterActivity, successMsg, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finishAffinity()
                }

                result.onFailure { e ->
                    val errorMsg = getString(R.string.error_prefix, friendlyError(e.message))
                    Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvGoLogin.setOnClickListener {
            finish()
        }
    }

    private fun updateRoleUI() {
        val isReader = binding.rbReader.isChecked
        val isAuthor = binding.rbAuthor.isChecked

        binding.layoutReader.setBackgroundResource(
            if (isReader) R.drawable.bg_role_card_selected else R.drawable.bg_role_card
        )
        binding.layoutAuthor.setBackgroundResource(
            if (isAuthor) R.drawable.bg_role_card_selected else R.drawable.bg_role_card
        )
    }

    private fun validate(name: String, email: String, password: String, confirm: String): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.err_empty_name)
            return false
        }
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.err_empty_email)
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.err_invalid_email)
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
        if (password != confirm) {
            binding.etConfirmPassword.error = getString(R.string.err_password_mismatch)
            return false
        }
        return true
    }

    private fun setLoading(loading: Boolean) {
        binding.btnRegister.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.text = if (loading) "" else getString(R.string.register)
    }

    private fun friendlyError(msg: String?): String {
        return when {
            msg == null -> getString(R.string.err_unknown)
            msg.contains("email-already-in-use") -> getString(R.string.err_email_in_use)
            msg.contains("weak-password")        -> getString(R.string.err_weak_password)
            msg.contains("invalid-email")        -> getString(R.string.err_invalid_email)
            msg.contains("network")              -> getString(R.string.err_network)
            else -> msg
        }
    }
}
