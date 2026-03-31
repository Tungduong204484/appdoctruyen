package com.example.appctruyn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appctruyn.model.Story

class StoryAdapter(
    private val storyList: List<Story>
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle:  TextView = itemView.findViewById(R.id.tv_title)
        val tvAuthor: TextView = itemView.findViewById(R.id.tv_author)
        val tvViews:  TextView = itemView.findViewById(R.id.tv_views)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]
        holder.tvTitle.text  = story.title
        holder.tvAuthor.text = story.author
        holder.tvViews.text  = "${story.views} lượt xem"
        holder.tvStatus.text = story.status
    }

    override fun getItemCount() = storyList.size
}