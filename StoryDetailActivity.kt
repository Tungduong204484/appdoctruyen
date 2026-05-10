package com.example.appctruyn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appctruyn.databinding.ActivityStoryDetailBinding
import com.example.appctruyn.model.Chapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra("storyId")
        if (storyId.isNullOrEmpty()) {
            Toast.makeText(this, "Không tìm thấy truyện", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupTabs()
        fetchStoryDetail(storyId)
        fetchChapters(storyId)
    }

    private fun setupTabs() {
        val tabTitles = listOf("Giới Thiệu", "Đánh Giá", "Bình Luận", "D.S Chương")
        tabTitles.forEach { title ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
        }

        // Mặc định hiển thị tab Giới Thiệu
        binding.layoutIntro.visibility = View.VISIBLE
        binding.rvChapters.visibility  = View.GONE

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { // Giới Thiệu
                        binding.layoutIntro.visibility = View.VISIBLE
                        binding.rvChapters.visibility  = View.GONE
                    }
                    3 -> { // D.S Chương
                        binding.layoutIntro.visibility = View.GONE
                        binding.rvChapters.visibility  = View.VISIBLE
                    }
                    else -> { // Đánh Giá, Bình Luận (chưa có nội dung)
                        binding.layoutIntro.visibility = View.GONE
                        binding.rvChapters.visibility  = View.GONE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun fetchStoryDetail(storyId: String) {
        db.collection("stories").document(storyId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Không tìm thấy truyện", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                binding.tvTitle.text       = doc.getString("title")       ?: "Không có tiêu đề"
                binding.tvAuthor.text      = doc.getString("author")      ?: "Không rõ tác giả"
                binding.tvStatus.text      = doc.getString("status")      ?: "Đang cập nhật"
                binding.tvGenre.text       = doc.getString("genre")       ?: "Chưa phân loại"
                binding.tvDescription.text = doc.getString("description") ?: "Chưa có mô tả"

                val rating = doc.getDouble("rating") ?: 0.0
                binding.tvRating.text  = String.format("%.1f", rating)
                binding.ratingBar.rating = rating.toFloat()

                val totalChap = doc.getLong("totalChapters")?.toInt() ?: 0
                binding.tvTotalChapters.text = "$totalChap chương - Còn tiếp"

                Glide.with(this)
                    .load(doc.getString("coverUrl"))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(binding.ivCover)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi tải truyện: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchChapters(storyId: String) {
        db.collection("stories").document(storyId)
            .collection("chapters")
            .orderBy("chapterNumber")
            .get()
            .addOnSuccessListener { result ->
                val chapters = result.documents.mapNotNull { doc ->
                    val chapterNumber = doc.getLong("chapterNumber")?.toInt()
                    val title         = doc.getString("title")
                    if (chapterNumber != null && title != null) {
                        Chapter(
                            id            = doc.id,
                            chapterNumber = chapterNumber,
                            title         = title,
                            content       = doc.getString("content") ?: "",
                            storyId       = storyId
                        )
                    } else null
                }

                if (chapters.isEmpty()) {
                    Toast.makeText(this, "Chưa có chương nào", Toast.LENGTH_SHORT).show()
                }

                setupChapterList(chapters)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi tải chương: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupChapterList(chapters: List<Chapter>) {
        val adapter = ChapterAdapter(chapters) { chapter ->
            val intent = Intent(this, ReadChapterActivity::class.java)
            intent.putExtra("chapterId",     chapter.id)
            intent.putExtra("storyId",       chapter.storyId)
            intent.putExtra("chapterNumber", chapter.chapterNumber)
            startActivity(intent)
        }
        binding.rvChapters.layoutManager = LinearLayoutManager(this)
        binding.rvChapters.adapter        = adapter
    }
}