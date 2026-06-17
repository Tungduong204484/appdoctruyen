package com.example.appctruyn;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appctruyn.databinding.ItemManageChapterBinding;
import com.example.appctruyn.model.Chapter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageChapterAdapter extends RecyclerView.Adapter<ManageChapterAdapter.ViewHolder> {

    private final List<Chapter> chapters;
    private final OnChapterActionListener listener;

    public interface OnChapterActionListener {
        void onEdit(Chapter chapter);
        void onDelete(Chapter chapter);
    }

    public ManageChapterAdapter(List<Chapter> chapters, OnChapterActionListener listener) {
        this.chapters = chapters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManageChapterBinding binding = ItemManageChapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        
        String title = chapter.getTitle();
        if (title == null || title.isEmpty()) {
            holder.binding.tvChapterTitle.setText(holder.itemView.getContext().getString(R.string.chapter_title_format, chapter.getNumber(), ""));
        } else {
            holder.binding.tvChapterTitle.setText(holder.itemView.getContext().getString(R.string.chapter_title_format, chapter.getNumber(), title));
        }

        String timestampStr = chapter.getTimestamp();
        if (timestampStr != null && !timestampStr.isEmpty()) {
            try {
                long timestampLong = Long.parseLong(timestampStr);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                holder.binding.tvTimestamp.setText(holder.itemView.getContext().getString(R.string.update_success) + ": " + sdf.format(new Date(timestampLong)));
            } catch (Exception e) {
                holder.binding.tvTimestamp.setText(timestampStr);
            }
        }

        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(chapter));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(chapter));
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemManageChapterBinding binding;
        ViewHolder(ItemManageChapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
