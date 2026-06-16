package com.example.appctruyn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.appctruyn.auth.AuthManager;
import com.example.appctruyn.model.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountFragment extends Fragment {

    private ImageView ivAvatar;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && isAdded()) {
                    processAndUploadAvatar(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView tvName = view.findViewById(R.id.tvUserName);
        final TextView tvEmail = view.findViewById(R.id.tvUserEmail);
        final TextView tvRoleBadge = view.findViewById(R.id.tvRoleBadge);
        final LinearLayout layoutAuthorMenu = view.findViewById(R.id.layoutAuthorMenu);
        final LinearLayout layoutAdminMenu = view.findViewById(R.id.layoutAdminMenu);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        View btnEditAvatar = view.findViewById(R.id.btnEditAvatar);

        btnEditAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        AuthManager.getCurrentUserInfo().addOnCompleteListener(task -> {
            if (!isAdded()) return;
            Context context = getContext();
            if (context == null) return;
            
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                
                if (tvName != null) {
                    String displayName = user.getDisplayName();
                    tvName.setText((displayName == null || displayName.isEmpty()) ? context.getString(R.string.user_default_name) : displayName);
                }
                if (tvEmail != null) tvEmail.setText(user.getEmail());

                loadAvatar(user.getAvatarUrl());

                String role = user.getRole();
                if (tvRoleBadge != null && layoutAuthorMenu != null && layoutAdminMenu != null) {
                    if ("author".equals(role)) {
                        tvRoleBadge.setText(context.getString(R.string.role_author_label));
                        tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_author);
                        layoutAuthorMenu.setVisibility(View.VISIBLE);
                        layoutAdminMenu.setVisibility(View.GONE);
                    } else if ("admin".equals(role)) {
                        tvRoleBadge.setText(context.getString(R.string.role_admin_label));
                        tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_admin);
                        layoutAuthorMenu.setVisibility(View.VISIBLE);
                        layoutAdminMenu.setVisibility(View.VISIBLE);
                    } else {
                        tvRoleBadge.setText(context.getString(R.string.role_reader_label));
                        tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_reader);
                        layoutAuthorMenu.setVisibility(View.GONE);
                        layoutAdminMenu.setVisibility(View.GONE);
                    }
                }
            }
        });

        btnLogout.setOnClickListener(v -> {
            Context context = getContext();
            if (context == null) return;
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.logout))
                    .setMessage(context.getString(R.string.logout_confirm))
                    .setPositiveButton(context.getString(R.string.logout), (dialog, which) -> {
                        AuthManager.logout();
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        if (getActivity() != null) getActivity().finishAffinity();
                    })
                    .setNegativeButton(context.getString(R.string.cancel), null)
                    .show();
        });
    }

    private void loadAvatar(String avatarData) {
        if (!isAdded() || ivAvatar == null) return;
        if (avatarData == null) {
            ivAvatar.setImageResource(R.drawable.ic_nav_account);
            return;
        }

        try {
            if (avatarData.startsWith("data:image") || isBase64(avatarData)) {
                String cleanBase64 = avatarData.contains(",") ? avatarData.split(",")[1] : avatarData;
                byte[] imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Glide.with(this).load(decodedImage).circleCrop().into(ivAvatar);
            } else {
                Glide.with(this).load(avatarData).placeholder(R.drawable.ic_nav_account).circleCrop().into(ivAvatar);
            }
        } catch (Exception e) {
            if (isAdded() && ivAvatar != null) ivAvatar.setImageResource(R.drawable.ic_nav_account);
        }
    }

    private boolean isBase64(String s) {
        try { Base64.decode(s, Base64.DEFAULT); return true; } catch (Exception e) { return false; }
    }

    private void processAndUploadAvatar(Uri uri) {
        executorService.execute(() -> {
            try {
                if (!isAdded()) return;
                String base64String = uriToBase64(uri);
                if (base64String != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        AuthManager.updateAvatarBase64(base64String).addOnCompleteListener(task -> {
                            if (!isAdded()) return;
                            Context context = getContext();
                            if (context == null) return;
                            
                            if (task.isSuccessful()) {
                                loadAvatar(base64String);
                                Toast.makeText(context, context.getString(R.string.update_avatar_success), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, context.getString(R.string.update_avatar_fail, "Error"), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            Context context = getContext();
                            if (context != null) Toast.makeText(context, context.getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private String uriToBase64(Uri uri) {
        try {
            if (!isAdded()) return null;
            Context context = getContext();
            if (context == null) return null;
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 400, (int)(bitmap.getHeight()*(400f/bitmap.getWidth())), false);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            return "data:image/jpeg;base64," + Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) { return null; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ivAvatar = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
