package com.example.appctruyn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appctruyn.auth.AuthManager;
import com.example.appctruyn.model.User;

import java.util.ArrayList;
import java.util.Arrays;

public class AdminManageUsersActivity extends AppCompatActivity {

    private UserAdapter adapter;
    private RecyclerView rvUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(new ArrayList<>(), this::showUserOptionsDialog);
        rvUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        AuthManager.getAllUsers().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                adapter.updateData(task.getResult());
            } else {
                Toast.makeText(AdminManageUsersActivity.this, getString(R.string.load_users_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserOptionsDialog(User user) {
        String[] options = {
                getString(R.string.edit_user),
                getString(R.string.delete_user),
                getString(R.string.change_role)
        };

        new AlertDialog.Builder(this)
                .setTitle(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditUserDialog(user);
                    } else if (which == 1) {
                        showDeleteConfirmDialog(user);
                    } else if (which == 2) {
                        showChangeRoleDialog(user);
                    }
                })
                .show();
    }

    private void showEditUserDialog(User user) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);
        EditText etName = view.findViewById(R.id.etEditName);
        EditText etEmail = view.findViewById(R.id.etEditEmail);

        etName.setText(user.getDisplayName());
        etEmail.setText(user.getEmail());

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_user)
                .setView(view)
                .setPositiveButton(R.string.update_success, (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();
                    if (!newName.isEmpty() && !newEmail.isEmpty()) {
                        updateUserInfo(user.getUid(), newName, newEmail);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateUserInfo(String uid, String name, String email) {
        AuthManager.adminUpdateUser(uid, name, email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(this, R.string.error_prefix, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_user_confirm_msg, user.getDisplayName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteUser(user.getUid());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteUser(String uid) {
        AuthManager.adminDeleteUser(uid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.delete_user_success, Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(this, R.string.delete_user_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangeRoleDialog(User user) {
        String[] roles = {"reader", "author", "admin"};
        int currentRoleIndex = Arrays.asList(roles).indexOf(user.getRole());

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.change_role_title, user.getDisplayName()))
                .setSingleChoiceItems(roles, currentRoleIndex, (dialog, which) -> {
                    String newRole = roles[which];
                    if (!newRole.equals(user.getRole())) {
                        updateUserRole(user.getUid(), newRole);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void updateUserRole(String uid, String newRole) {
        AuthManager.updateUserRole(uid, newRole).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AdminManageUsersActivity.this, getString(R.string.update_role_success), Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(AdminManageUsersActivity.this, getString(R.string.update_role_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
