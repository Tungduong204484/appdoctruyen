package com.example.appctruyn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appctruyn.model.Story;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<Story> bannerList;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(Story story);
    }

    public BannerAdapter(List<Story> bannerList, OnItemClickListener onItemClick) {
        this.bannerList = bannerList;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        holder.bind(bannerList.get(position));
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    public class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivBanner;

        public BannerViewHolder(@NonNull View view) {
            super(view);
            ivBanner = view.findViewById(R.id.ivBanner);
        }

        public void bind(final Story story) {
            Glide.with(itemView.getContext())
                    .load(story.getCoverUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivBanner);

            itemView.setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.onItemClick(story);
                }
            });
        }
    }
}
