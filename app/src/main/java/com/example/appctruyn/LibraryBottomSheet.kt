package com.example.appctruyn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.appctruyn.model.LibraryStory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LibraryBottomSheet : BottomSheetDialogFragment() {

    var onDeleteClick: (() -> Unit)? = null
    var onBookmarkToggle: ((Boolean) -> Unit)? = null

    companion object {
        private const val ARG_STORY_ID = "storyId"
        private const val ARG_TITLE = "title"
        private const val ARG_COVER = "coverUrl"
        private const val ARG_LAST_CHAP = "lastChap"
        private const val ARG_TOTAL_CHAP = "totalChap"
        private const val ARG_NOTIFY = "notifyEnabled"

        fun newInstance(item: LibraryStory): LibraryBottomSheet {
            val f = LibraryBottomSheet()
            f.arguments = Bundle().apply {
                putString(ARG_STORY_ID, item.storyId)
                putString(ARG_TITLE, item.title)
                putString(ARG_COVER, item.coverUrl)
                putInt(ARG_LAST_CHAP, item.lastChap)
                putInt(ARG_TOTAL_CHAP, item.totalChap)
                putBoolean(ARG_NOTIFY, item.notifyEnabled)
            }
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_library, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TITLE) ?: ""
        val coverUrl = arguments?.getString(ARG_COVER) ?: ""
        var notifyEnabled = arguments?.getBoolean(ARG_NOTIFY) ?: false

        val ivCover = view.findViewById<ImageView>(R.id.ivCover)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val btnDownload = view.findViewById<View>(R.id.btnDownload)
        val btnDelete = view.findViewById<View>(R.id.btnDelete)
        val switchNotify = view.findViewById<Switch>(R.id.switchNotify)

        tvTitle.text = title
        switchNotify.isChecked = notifyEnabled

        Glide.with(requireContext()).load(coverUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(ivCover)

        btnDownload.setOnClickListener {
            // Tính năng tải truyện (sắp ra mắt)
            dismiss()
        }

        btnDelete.setOnClickListener {
            onDeleteClick?.invoke()
            dismiss()
        }

        switchNotify.setOnCheckedChangeListener { _, isChecked ->
            notifyEnabled = isChecked
            onBookmarkToggle?.invoke(isChecked)
        }
    }
}