package com.example.appctruyn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.appctruyn.databinding.ItemManageStoryBinding;
import com.example.appctruyn.model.Story;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ManageStoryAdapter extends RecyclerView.Adapter<ManageStoryAdapter.ViewHolder> {

    private final List<Story> stories;
    private final OnStoryActionListener listener;
    private final boolean isAdminMode;

    public interface OnStoryActionListener {
        void onEdit(Story story);
        void onDelete(Story story);
        void onClick(Story story);
        void onAddChapter(Story story);
    }

    public ManageStoryAdapter(List<Story> stories, boolean isAdminMode, OnStoryActionListener listener) {
        this.stories = stories;
        this.isAdminMode = isAdminMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManageStoryBinding binding = ItemManageStoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = stories.get(position);
        holder.binding.tvTitle.setText(story.getTitle());
        
        if (isAdminMode) {
            holder.binding.tvAuthorInfo.setText("Đăng bởi: " + story.getAuthor());
        } else {
            holder.binding.tvAuthorInfo.setText("Tác giả: " + story.getAuthor());
        }

        if (story.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.binding.tvDate.setText("Ngày đăng: " + sdf.format(story.getCreatedAt()));
        } else {
            holder.binding.tvDate.setText("Ngày đăng: --/--/----");
        }

        Glide.with(holder.itemView.getContext())
                .load(story.getCoverUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.binding.ivCover);

        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(story));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(story));
        holder.binding.btnAddChapter.setOnClickListener(v -> listener.onAddChapter(story));
        holder.itemView.setOnClickListener(v -> listener.onClick(story));
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemManageStoryBinding binding;
        ViewHolder(ItemManageStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
