package com.example.appctruyn

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appctruyn.auth.AuthManager
import com.example.appctruyn.model.User
import kotlinx.coroutines.launch

class AdminManageUsersActivity : AppCompatActivity() {

    private lateinit var adapter: UserAdapter
    private lateinit var rvUsers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_manage_users)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvUsers = findViewById(R.id.rvUsers)
        rvUsers.layoutManager = LinearLayoutManager(this)

        adapter = UserAdapter(emptyList()) { user ->
            showUserOptionsDialog(user)
        }
        rvUsers.setAdapter(adapter)

        loadUsers()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            AuthManager.getAllUsers().onSuccess { users ->
                adapter.updateData(users)
            }.onFailure {
                Toast.makeText(this@AdminManageUsersActivity, getString(R.string.load_users_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUserOptionsDialog(user: User) {
        val roles = arrayOf("reader", "author", "admin")
        val currentRoleIndex = roles.indexOf(user.role)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.change_role_title, user.displayName))
            .setSingleChoiceItems(roles, currentRoleIndex) { dialog, which ->
                val newRole = roles[which]
                if (newRole != user.role) {
                    updateUserRole(user.uid, newRole)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateUserRole(uid: String, newRole: String) {
        lifecycleScope.launch {
            AuthManager.updateUserRole(uid, newRole).onSuccess {
                Toast.makeText(this@AdminManageUsersActivity, getString(R.string.update_role_success), Toast.LENGTH_SHORT).show()
                loadUsers()
            }.onFailure {
                Toast.makeText(this@AdminManageUsersActivity, getString(R.string.update_role_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
