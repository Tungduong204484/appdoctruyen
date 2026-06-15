package com.example.appctruyn;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appctruyn.databinding.ItemStoryBinding;
import com.example.appctruyn.model.Story;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final List<Story> storyList;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(Story story);
    }

    public StoryAdapter(List<Story> storyList, OnItemClickListener onItemClick) {
        this.storyList = storyList;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryBinding binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        holder.bind(storyList.get(position));
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemStoryBinding binding;

        public StoryViewHolder(ItemStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Story story) {
            binding.tvTitle.setText(story.getTitle());
            binding.tvGenre.setText(story.getGenre());
            binding.tvViews.setText(binding.getRoot().getContext().getString(R.string.views_format, story.getViews()));
            binding.tvStatus.setText(story.getStatus());

            // Load ảnh với Glide
            Glide.with(binding.getRoot().getContext())
                    .load(story.getCoverUrl())
                    .into(binding.ivCover);

            binding.getRoot().setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.onItemClick(story);
                }
            });
        }
    }
}
