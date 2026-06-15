package com.example.appctruyn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appctruyn.model.Story;

import java.util.List;

public class DeCuAdapter extends RecyclerView.Adapter<DeCuAdapter.ViewHolder> {

    private final List<Story> storyList;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(Story story);
    }

    public DeCuAdapter(List<Story> storyList, OnItemClickListener onItemClick) {
        this.storyList = storyList;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_de_cu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(storyList.get(position));
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvGenre;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivCover = view.findViewById(R.id.ivCover);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvGenre = view.findViewById(R.id.tvGenre);
        }

        public void bind(final Story story) {
            tvTitle.setText(story.getTitle());
            tvGenre.setText(story.getGenre());
            Glide.with(itemView.getContext())
                    .load(story.getCoverUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivCover);

            itemView.setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.onItemClick(story);
                }
            });
        }
    }
}
