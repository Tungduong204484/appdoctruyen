package com.example.appctruyn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appctruyn.model.Story

class DeCuAdapter(private val storyList: List<Story>) :
    RecyclerView.Adapter<DeCuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvGenre: TextView = view.findViewById(R.id.tvGenre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_de_cu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = storyList[position]
        holder.tvTitle.text = story.title
        holder.tvGenre.text = story.genre
        Glide.with(holder.itemView.context)
            .load(story.coverUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.ivCover)
    }

    override fun getItemCount() = storyList.size
}
