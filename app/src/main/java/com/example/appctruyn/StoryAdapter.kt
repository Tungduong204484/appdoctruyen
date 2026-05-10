package com.example.appctruyn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appctruyn.databinding.ItemStoryBinding
import com.example.appctruyn.model.Story

class StoryAdapter(
    private val storyList: List<Story>,
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]
        holder.bind(story)
    }

    override fun getItemCount(): Int = storyList.size

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story) {
            binding.tvTitle.text = story.title
            binding.tvGenre.text = story.genre
            binding.tvViews.text = binding.root.context.getString(R.string.views_format, story.views)
            binding.tvStatus.text = story.status

            // Load ảnh với Glide
            Glide.with(binding.root.context)
                .load(story.coverUrl)
                .into(binding.ivCover)

            binding.root.setOnClickListener {
                onItemClick(story)
            }
        }
    }
}