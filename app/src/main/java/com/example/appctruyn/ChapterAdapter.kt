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
        holder.binding.apply {
            // Hiển thị số thứ tự chương (1, 2, 3...)
            tvChapterIndex.text = (position + 1).toString()
            
            // Hiển thị tiêu đề: "Chương X: Tên chương"
            tvChapterTitle.text = "Chương ${chapter.number}: ${chapter.title}"
            
            // Hiển thị thời gian đăng chương (ví dụ: (2025-02-19 09:43))
            tvTimestamp.text = if (chapter.timestamp.isNotEmpty()) "(${chapter.timestamp})" else ""
            
            // Sự kiện click để mở màn hình đọc truyện
            root.setOnClickListener { onItemClick(chapter) }
        }
    }

    override fun getItemCount(): Int = chapters.size
}