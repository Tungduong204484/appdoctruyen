package com.example.appctruyn

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appctruyn.model.Story

class RankingAdapter(
    private val storyList: List<Story>,
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvValue: TextView = view.findViewById(R.id.tvValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = storyList[position]
        val rank = position + 1
        
        holder.tvRank.text = rank.toString()
        holder.tvTitle.text = story.title
        holder.tvAuthor.text = story.author
        holder.tvCategory.text = "#${story.genre.uppercase()}"
        holder.tvRating.text = "★ ${story.rating}"
        holder.tvValue.text = story.views.toString()

        // Rank colors (1, 2, 3)
        val bg = holder.tvRank.background as GradientDrawable
        when (rank) {
            1 -> bg.setColor(Color.parseColor("#FFB300"))
            2 -> bg.setColor(Color.parseColor("#FB8C00"))
            3 -> bg.setColor(Color.parseColor("#F4511E"))
            else -> bg.setColor(Color.parseColor("#BDBDBD"))
        }

        Glide.with(holder.itemView.context)
            .load(story.coverUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.ivCover)

        holder.itemView.setOnClickListener {
            onItemClick(story)
        }
    }

    override fun getItemCount() = storyList.size
}
