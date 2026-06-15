package com.example.appctruyn;

import android.os.Bundle;
import android.view.MenuItem;
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
import java.util.List;

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
