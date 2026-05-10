package com.example.appctruyn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appctruyn.databinding.ItemChapterBinding
import com.example.appctruyn.model.Chapter

class ChapterAdapter(
    private val chapters: List<Chapter>,
    private val onItemClick: (Chapter) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.binding.tvChapterTitle.text = "Chương ${chapter.chapterNumber}: ${chapter.title}"
        holder.binding.root.setOnClickListener { onItemClick(chapter) }
    }

    override fun getItemCount(): Int = chapters.size
}