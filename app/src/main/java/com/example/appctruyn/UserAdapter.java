package com.example.appctruyn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appctruyn.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private final OnUserClickListener onUserClick;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> users, OnUserClickListener onUserClick) {
        this.users = users;
        this.onUserClick = onUserClick;
    }

    public void updateData(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAvatar;
        private final TextView tvName;
        private final TextView tvEmail;
        private final TextView tvRole;

        public UserViewHolder(@NonNull View view) {
            super(view);
            ivAvatar = view.findViewById(R.id.ivUserAvatar);
            tvName = view.findViewById(R.id.tvUserName);
            tvEmail = view.findViewById(R.id.tvUserEmail);
            tvRole = view.findViewById(R.id.tvUserRole);
        }

        public void bind(final User user) {
            String displayName = user.getDisplayName();
            tvName.setText((displayName == null || displayName.isEmpty()) ? "Người dùng" : displayName);
            tvEmail.setText(user.getEmail());
            
            String role = user.getRole();
            tvRole.setText(role != null ? role.toUpperCase() : "");

            String avatarData = user.getAvatarUrl();
            if (avatarData != null) {
                if (avatarData.startsWith("data:image")) {
                    try {
                        String cleanBase64 = avatarData.contains(",") ? avatarData.split(",")[1] : avatarData;
                        byte[] imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        Glide.with(itemView.getContext())
                            .load(decodedImage)
                            .placeholder(R.drawable.ic_nav_account)
                            .circleCrop()
                            .into(ivAvatar);
                    } catch (Exception e) {
                        ivAvatar.setImageResource(R.drawable.ic_nav_account);
                    }
                } else {
                    // Trường hợp là URL
                    Glide.with(itemView.getContext())
                        .load(avatarData)
                        .placeholder(R.drawable.ic_nav_account)
                        .circleCrop()
                        .into(ivAvatar);
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_nav_account);
            }

            itemView.setOnClickListener(v -> {
                if (onUserClick != null) {
                    onUserClick.onUserClick(user);
                }
            });
        }
    }
}
