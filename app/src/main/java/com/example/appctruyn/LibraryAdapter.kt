package com.example.appctruyn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appctruyn.model.LibraryStory

class LibraryAdapter(
    private val items: List<LibraryStory>,
    private val onItemClick: (LibraryStory) -> Unit,
    private val onMenuClick: (LibraryStory, View) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvProgress: TextView = view.findViewById(R.id.tvProgress)
        val btnNotify: ImageView = view.findViewById(R.id.btnNotify)
        val btnMenu: ImageButton = view.findViewById(R.id.btnMenu)

        fun bind(item: LibraryStory) {
            tvTitle.text = item.title
            // Hiển thị "Đã đọc X/Y" giống app gốc
            tvProgress.text = if (item.totalChap > 0)
                "Đã đọc ${item.lastChap}/${item.totalChap}"
            else
                "Đã đọc chương ${item.lastChap}"

            // Icon chuông (notify)
            btnNotify.setImageResource(
                if (item.notifyEnabled) R.drawable.ic_notify_on
                else R.drawable.ic_notify_off
            )

            Glide.with(itemView.context)
                .load(item.coverUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivCover)

            itemView.setOnClickListener { onItemClick(item) }
            btnMenu.setOnClickListener { onMenuClick(item, it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}