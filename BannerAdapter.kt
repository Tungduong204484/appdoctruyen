package com.example.appctruyn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appctruyn.R
import com.example.appctruyn.model.Story

class BannerAdapter(
    private val bannerList: List<Story>,
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBanner: ImageView = view.findViewById(R.id.ivBanner)

        fun bind(story: Story) {
            // Load ảnh
            Glide.with(itemView.context)
                .load(story.coverUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivBanner)

            // Xử lý click
            itemView.setOnClickListener {
                onItemClick(story)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val story = bannerList[position]
        holder.bind(story)
    }

    override fun getItemCount(): Int = bannerList.size
}