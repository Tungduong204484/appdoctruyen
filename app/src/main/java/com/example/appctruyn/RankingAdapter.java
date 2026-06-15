package com.example.appctruyn;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private final List<Story> storyList;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(Story story);
    }

    public RankingAdapter(List<Story> storyList, OnItemClickListener onItemClick) {
        this.storyList = storyList;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = storyList.get(position);
        int rank = position + 1;

        holder.tvRank.setText(String.valueOf(rank));
        holder.tvTitle.setText(story.getTitle());
        holder.tvAuthor.setText(story.getAuthor());
        
        String genre = story.getGenre();
        holder.tvCategory.setText(holder.itemView.getContext().getString(R.string.rank_genre_format, genre != null ? genre.toUpperCase() : ""));
        
        holder.tvRating.setText(holder.itemView.getContext().getString(R.string.rank_rating_format, story.getRating()));
        holder.tvValue.setText(String.valueOf(story.getViews()));

        // Rank colors (1, 2, 3)
        GradientDrawable bg = (GradientDrawable) holder.tvRank.getBackground();
        if (bg != null) {
            switch (rank) {
                case 1:
                    bg.setColor(Color.parseColor("#FFB300"));
                    break;
                case 2:
                    bg.setColor(Color.parseColor("#FB8C00"));
                    break;
                case 3:
                    bg.setColor(Color.parseColor("#F4511E"));
                    break;
                default:
                    bg.setColor(Color.parseColor("#BDBDBD"));
                    break;
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(story.getCoverUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onItemClick(story);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivCover;
        final TextView tvCategory;
        final TextView tvRank;
        final TextView tvTitle;
        final TextView tvAuthor;
        final TextView tvRating;
        final TextView tvValue;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivCover = view.findViewById(R.id.ivCover);
            tvCategory = view.findViewById(R.id.tvCategory);
            tvRank = view.findViewById(R.id.tvRank);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvAuthor = view.findViewById(R.id.tvAuthor);
            tvRating = view.findViewById(R.id.tvRating);
            tvValue = view.findViewById(R.id.tvValue);
        }
    }
}
