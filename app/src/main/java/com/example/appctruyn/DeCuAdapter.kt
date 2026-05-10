package com.example.appctruyn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appctruyn.R
import com.example.appctruyn.model.Story

class DeCuAdapter(
    private val storyList: List<Story>,
    private val onItemClick: (Story) -> Unit  // ← PHẢI CÓ
) : RecyclerView.Adapter<DeCuAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvGenre: TextView = view.findViewById(R.id.tvGenre)

        fun bind(story: Story) {
            tvTitle.text = story.title
            tvGenre.text = story.genre
            Glide.with(itemView.context)
                .load(story.coverUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivCover)

            // ← PHẢI CÓ CLICK LISTENER NÀY
            itemView.setOnClickListener {
                onItemClick(story)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_de_cu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(storyList[position])
    }

    override fun getItemCount(): Int = storyList.size
}