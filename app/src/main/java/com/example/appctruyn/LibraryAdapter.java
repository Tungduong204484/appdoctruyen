package com.example.appctruyn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appctruyn.model.LibraryStory;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private final List<LibraryStory> items;
    private final OnItemClickListener onItemClick;
    private final OnMenuClickListener onMenuClick;

    public interface OnItemClickListener {
        void onItemClick(LibraryStory item);
    }

    public interface OnMenuClickListener {
        void onMenuClick(LibraryStory item, View view);
    }

    public LibraryAdapter(List<LibraryStory> items, OnItemClickListener onItemClick, OnMenuClickListener onMenuClick) {
        this.items = items;
        this.onItemClick = onItemClick;
        this.onMenuClick = onMenuClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvProgress;
        private final ImageView btnNotify;
        private final ImageButton btnMenu;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivCover = view.findViewById(R.id.ivCover);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvProgress = view.findViewById(R.id.tvProgress);
            btnNotify = view.findViewById(R.id.btnNotify);
            btnMenu = view.findViewById(R.id.btnMenu);
        }

        public void bind(final LibraryStory item) {
            tvTitle.setText(item.getTitle());
            
            if (item.getTotalChap() > 0) {
                tvProgress.setText(itemView.getContext().getString(R.string.library_progress, item.getLastChap(), item.getTotalChap()));
            } else {
                tvProgress.setText(itemView.getContext().getString(R.string.library_progress_single, item.getLastChap()));
            }

            btnNotify.setImageResource(
                    item.isNotifyEnabled() ? R.drawable.ic_notify_on : R.drawable.ic_notify_off
            );

            Glide.with(itemView.getContext())
                    .load(item.getCoverUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivCover);

            itemView.setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.onItemClick(item);
                }
            });

            btnMenu.setOnClickListener(v -> {
                if (onMenuClick != null) {
                    onMenuClick.onMenuClick(item, v);
                }
            });
        }
    }
}
