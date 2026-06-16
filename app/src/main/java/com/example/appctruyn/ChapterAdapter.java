package com.example.appctruyn;

import android.view.LayoutInflater;
import android.view.View;
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
        
        // Display chapter index (1, 2, 3...)
        holder.binding.tvChapterIndex.setText(String.valueOf(position + 1));
        
        // Display title using string resource: "Chương %d: %s"
        String title = chapter.getTitle();
        if (title == null || title.isEmpty()) {
            title = holder.itemView.getContext().getString(R.string.no_title);
        }
        
        holder.binding.tvChapterTitle.setText(
            holder.itemView.getContext().getString(R.string.chapter_title_format, chapter.getNumber(), title)
        );
        
        // Display timestamp if available
        String timestamp = chapter.getTimestamp();
        if (timestamp != null && !timestamp.isEmpty()) {
            holder.binding.tvTimestamp.setVisibility(View.VISIBLE);
            holder.binding.tvTimestamp.setText(timestamp);
        } else {
            holder.binding.tvTimestamp.setVisibility(View.GONE);
        }
        
        // Click listener for reading
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
