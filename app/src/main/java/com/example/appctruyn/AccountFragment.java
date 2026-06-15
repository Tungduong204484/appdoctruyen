package com.example.appctruyn;

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

    private TextView tvCurrentTheme;
    private ImageView ivAvatar;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
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

        TextView tvName = view.findViewById(R.id.tvUserName);
        TextView tvEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvRoleBadge = view.findViewById(R.id.tvRoleBadge);
        LinearLayout layoutAuthorMenu = view.findViewById(R.id.layoutAuthorMenu);
        LinearLayout layoutAdminMenu = view.findViewById(R.id.layoutAdminMenu);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        View btnEditAvatar = view.findViewById(R.id.btnEditAvatar);

        tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme);
        View btnThemeMode = view.findViewById(R.id.btnThemeMode);

        updateThemeText();

        btnThemeMode.setOnClickListener(v -> showThemeSelectionDialog());

        btnEditAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        AuthManager.getCurrentUserInfo().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                String displayName = user.getDisplayName();
                tvName.setText((displayName == null || displayName.isEmpty()) ? getString(R.string.user_default_name) : displayName);
                tvEmail.setText(user.getEmail());

                loadAvatar(user.getAvatarUrl());

                String role = user.getRole();
                if ("author".equals(role)) {
                    tvRoleBadge.setText(getString(R.string.role_author_label));
                    tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_author);
                    layoutAuthorMenu.setVisibility(View.VISIBLE);
                    layoutAdminMenu.setVisibility(View.GONE);
                } else if ("admin".equals(role)) {
                    tvRoleBadge.setText(getString(R.string.role_admin_label));
                    tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_admin);
                    layoutAuthorMenu.setVisibility(View.VISIBLE);
                    layoutAdminMenu.setVisibility(View.VISIBLE);
                } else {
                    tvRoleBadge.setText(getString(R.string.role_reader_label));
                    tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_reader);
                    layoutAuthorMenu.setVisibility(View.GONE);
                    layoutAdminMenu.setVisibility(View.GONE);
                }
            }
        });

        View btnManageUsers = view.findViewById(R.id.btnManageUsers);
        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), AdminManageUsersActivity.class));
            });
        }

        View btnMyStories = view.findViewById(R.id.btnMyStories);
        if (btnMyStories != null) {
            btnMyStories.setOnClickListener(v -> {
                Toast.makeText(requireContext(), getString(R.string.manage_stories_coming_soon), Toast.LENGTH_SHORT).show();
            });
        }

        View btnAddStory = view.findViewById(R.id.btnAddStory);
        if (btnAddStory != null) {
            btnAddStory.setOnClickListener(v -> {
                Toast.makeText(requireContext(), getString(R.string.add_story_coming_soon), Toast.LENGTH_SHORT).show();
            });
        }

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.logout_confirm))
                    .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                        AuthManager.logout();
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                        requireActivity().finishAffinity();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });
    }

    private void loadAvatar(String avatarData) {
        if (avatarData == null) {
            ivAvatar.setImageResource(R.drawable.ic_nav_account);
            return;
        }

        if (avatarData.startsWith("data:image") || isBase64(avatarData)) {
            String cleanBase64 = avatarData.contains(",") ? avatarData.split(",")[1] : avatarData;
            try {
                byte[] imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Glide.with(this).load(decodedImage).circleCrop().into(ivAvatar);
            } catch (Exception e) {
                ivAvatar.setImageResource(R.drawable.ic_nav_account);
            }
        } else {
            Glide.with(this).load(avatarData).placeholder(R.drawable.ic_nav_account).circleCrop().into(ivAvatar);
        }
    }

    private boolean isBase64(String s) {
        try {
            Base64.decode(s, Base64.DEFAULT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void processAndUploadAvatar(Uri uri) {
        executorService.execute(() -> {
            try {
                String base64String = uriToBase64(uri);
                if (base64String != null) {
                    getActivity().runOnUiThread(() -> {
                        AuthManager.updateAvatarBase64(base64String).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                loadAvatar(base64String);
                                Toast.makeText(requireContext(), getString(R.string.update_avatar_success), Toast.LENGTH_SHORT).show();
                            } else {
                                String errorMsg = getString(R.string.update_avatar_fail, task.getException().getMessage());
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), getString(R.string.err_unknown), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap resizedBitmap = resizeBitmap(bitmap, 400);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap resizeBitmap(Bitmap source, int maxLength) {
        int outWidth;
        int outHeight;
        int inWidth = source.getWidth();
        int inHeight = source.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxLength;
            outHeight = (int) ((float) inHeight / inWidth * maxLength);
        } else {
            outHeight = maxLength;
            outWidth = (int) ((float) inWidth / inHeight * maxLength);
        }
        return Bitmap.createScaledBitmap(source, outWidth, outHeight, false);
    }

    private void updateThemeText() {
        int currentMode = ThemeHelper.getThemePreference(requireContext());
        tvCurrentTheme.setText(ThemeHelper.getThemeName(requireContext(), currentMode));
    }

    private void showThemeSelectionDialog() {
        String[] options = {
                getString(R.string.theme_light),
                getString(R.string.theme_dark),
                getString(R.string.theme_auto)
        };
        int currentMode = ThemeHelper.getThemePreference(requireContext());

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.theme_selection))
                .setSingleChoiceItems(options, currentMode, (dialog, which) -> {
                    ThemeHelper.setThemePreference(requireContext(), which);
                    updateThemeText();
                    dialog.dismiss();
                    requireActivity().recreate();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
