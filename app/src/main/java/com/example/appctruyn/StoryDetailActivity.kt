package com.example.appctruyn

import android.content.Context
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
    private var firstChapterId: String? = null
    private var storyId: String? = null
    private var lastChapterId: String? = null
    private var lastChapterNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storyId = intent.getStringExtra("storyId")
        if (storyId.isNullOrEmpty()) {
            Toast.makeText(this, "Không tìm thấy truyện", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupTabs()
        fetchStoryDetail(storyId!!)
        fetchChapters(storyId!!)
    }

    override fun onResume() {
        super.onResume()
        updateReadButtonState()
    }

    private fun updateReadButtonState() {
        val prefs = getSharedPreferences("ReaderPrefs", Context.MODE_PRIVATE)
        lastChapterId = prefs.getString("last_chapter_id_$storyId", null)
        lastChapterNumber = prefs.getInt("last_chapter_number_$storyId", 1)

        if (lastChapterId != null) {
            binding.btnRead.text = "Đọc tiếp C.$lastChapterNumber"
        } else {
            binding.btnRead.text = "Đọc truyện"
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnRead.setOnClickListener {
            val targetChapterId = lastChapterId ?: firstChapterId
            val targetChapterNumber = if (lastChapterId != null) lastChapterNumber else 1

            if (targetChapterId != null) {
                val intent = Intent(this, ReadChapterActivity::class.java)
                intent.putExtra("chapterId", targetChapterId)
                intent.putExtra("storyId", storyId)
                intent.putExtra("chapterNumber", targetChapterNumber)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Truyện chưa có chương", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnAddLibrary.setOnClickListener {
            Toast.makeText(this, "Đã thêm vào tủ truyện", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTabs() {
        val tabTitles = listOf("Giới Thiệu", "Đánh Giá", "Bình Luận", "D.S Chương")
        tabTitles.forEach { title ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { // Giới Thiệu
                        binding.layoutIntro.visibility = View.VISIBLE
                        binding.layoutChapters.visibility = View.GONE
                    }
                    3 -> { // D.S Chương
                        binding.layoutIntro.visibility = View.GONE
                        binding.layoutChapters.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.layoutIntro.visibility = View.GONE
                        binding.layoutChapters.visibility = View.GONE
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

                binding.tvTitle.text       = doc.getString("title") ?: "Không có tiêu đề"
                binding.tvAuthor.text      = doc.getString("author") ?: "Không rõ tác giả"
                binding.tvGenre.text       = doc.getString("genre") ?: "Chưa phân loại"
                binding.tvDescription.text = doc.getString("description") ?: "Chưa có mô tả"

                val rating = doc.getDouble("rating") ?: 0.0
                binding.tvRating.text = String.format("%.1f", rating)
                binding.ratingBar.rating = rating.toFloat()

                val totalChap = doc.getLong("totalChapters")?.toInt() ?: 0
                binding.tvChapterCountStats.text = totalChap.toString()
                binding.tvStatusStats.text = "Chương - " + (doc.getString("status") ?: "Còn tiếp")
                
                binding.tvViewCountStats.text = (doc.getLong("views") ?: 0).toString()

                Glide.with(this)
                    .load(doc.getString("coverUrl"))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(binding.ivCover)
            }
    }

    private fun fetchChapters(storyId: String) {
        db.collection("stories").document(storyId)
            .collection("chapters")
            .orderBy("number")
            .get()
            .addOnSuccessListener { result ->
                val chapters = result.documents.mapNotNull { doc ->
                    val chapter = doc.toObject(Chapter::class.java)
                    chapter?.id = doc.id
                    chapter?.storyId = storyId
                    chapter
                }

                if (chapters.isNotEmpty()) {
                    firstChapterId = chapters[0].id
                    binding.tvChapterTitleHeader.text = "Số chương (${chapters.size})"
                    binding.tvChapterCountStats.text = chapters.size.toString()
                }

                setupChapterList(chapters)
            }
            .addOnFailureListener {
                // Fallback nếu có lỗi hoặc cấu trúc khác
                db.collection("stories").document(storyId)
                    .collection("chapters")
                    .get()
                    .addOnSuccessListener { result ->
                         val chapters = result.documents.mapNotNull { doc ->
                            val number = doc.getLong("number")?.toInt() ?: doc.getLong("chapterNumber")?.toInt() ?: 0
                            Chapter(
                                id = doc.id,
                                storyId = storyId,
                                title = doc.getString("title") ?: "",
                                number = number,
                                content = doc.getString("content") ?: "",
                                timestamp = doc.getString("timestamp") ?: ""
                            )
                        }
                        if (chapters.isNotEmpty()) {
                            firstChapterId = chapters[0].id
                        }
                        setupChapterList(chapters)
                    }
            }
    }

    private fun setupChapterList(chapters: List<Chapter>) {
        val adapter = ChapterAdapter(chapters) { chapter ->
            val intent = Intent(this, ReadChapterActivity::class.java)
            intent.putExtra("chapterId", chapter.id)
            intent.putExtra("storyId", chapter.storyId)
            intent.putExtra("chapterNumber", chapter.number)
            startActivity(intent)
        }
        binding.rvChapters.layoutManager = LinearLayoutManager(this)
        binding.rvChapters.adapter = adapter
    }
}