package com.example.appctruyn

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appctruyn.databinding.ActivityReadChapterBinding
import com.google.firebase.firestore.FirebaseFirestore

class ReadChapterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadChapterBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadChapterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chapterId = intent.getStringExtra("chapterId")
        val chapterNumber = intent.getIntExtra("chapterNumber", 0)
        val storyId = intent.getStringExtra("storyId")

        if (chapterId == null || storyId == null) {
            Toast.makeText(this, "Không tìm thấy chương", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Lưu chương đang đọc vào SharedPreferences
        saveLastReadChapter(storyId, chapterId, chapterNumber)

        loadChapter(storyId, chapterId, chapterNumber)
    }

    private fun saveLastReadChapter(storyId: String, chapterId: String, chapterNumber: Int) {
        val prefs = getSharedPreferences("ReaderPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("last_chapter_id_$storyId", chapterId)
            putInt("last_chapter_number_$storyId", chapterNumber)
            apply()
        }
    }

    private fun loadChapter(storyId: String, chapterId: String, chapterNumber: Int) {
        db.collection("stories").document(storyId)
            .collection("chapters").document(chapterId)
            .get()
            .addOnSuccessListener { doc ->
                val title = doc.getString("title") ?: "Không có tiêu đề"
                val content = doc.getString("content") ?: "Nội dung đang được cập nhật..."

                binding.tvChapterTitle.text = "Chương $chapterNumber: $title"
                binding.tvChapterContent.text = content
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải nội dung chương", Toast.LENGTH_SHORT).show()
            }
    }
}