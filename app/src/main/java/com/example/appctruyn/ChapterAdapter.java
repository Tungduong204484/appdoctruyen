package com.example.appctruyn;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appctruyn.databinding.ItemChapterBinding;
import com.example.appctruyn.model.Chapter;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {

    private final List<Chapter> chapters;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(Chapter chapter);
    }

    public ChapterAdapter(List<Chapter> chapters, OnItemClickListener onItemClick) {
        this.chapters = chapters;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChapterBinding binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        
        // Hiển thị số thứ tự chương (1, 2, 3...)
        holder.binding.tvChapterIndex.setText(String.valueOf(position + 1));
        
        // Hiển thị tiêu đề: "Chương X: Tên chương"
        String titleText = "Chương " + chapter.getNumber() + ": " + chapter.getTitle();
        holder.binding.tvChapterTitle.setText(titleText);
        
        // Hiển thị thời gian đăng chương
        String timestamp = chapter.getTimestamp();
        holder.binding.tvTimestamp.setText((timestamp != null && !timestamp.isEmpty()) ? "(" + timestamp + ")" : "");
        
        // Sự kiện click để mở màn hình đọc truyện
        holder.binding.getRoot().setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onItemClick(chapter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemChapterBinding binding;

        public ViewHolder(ItemChapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
