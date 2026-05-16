package com.example.appctruyn

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appctruyn.databinding.ActivityReadChapterBinding
import com.example.appctruyn.databinding.DialogReadingSettingsBinding
import com.example.appctruyn.model.Chapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReadChapterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadChapterBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentStoryId: String? = null
    private var currentChapterNumber: Int = 0
    
    private var textSize: Float = 18f
    private var isSerif: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadChapterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentStoryId = intent.getStringExtra("storyId")
        currentChapterNumber = intent.getIntExtra("chapterNumber", 0)
        val chapterId = intent.getStringExtra("chapterId")

        if (currentStoryId == null || (chapterId == null && currentChapterNumber == 0)) {
            Toast.makeText(this, getString(R.string.story_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPreferences()
        applyReadingSettings()
        setupListeners()

        if (chapterId != null) {
            loadChapterById(chapterId)
        } else {
            loadChapterByNumber(currentChapterNumber)
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        binding.btnPrevChapter.setOnClickListener {
            navigateToChapter(currentChapterNumber - 1)
        }

        binding.btnNextChapter.setOnClickListener {
            navigateToChapter(currentChapterNumber + 1)
        }
    }

    private fun navigateToChapter(number: Int) {
        if (number < 1) {
            Toast.makeText(this, "Đây là chương đầu tiên", Toast.LENGTH_SHORT).show()
            return
        }
        loadChapterByNumber(number)
    }

    private fun loadChapterByNumber(number: Int) {
        binding.progressBar.visibility = View.VISIBLE
        currentStoryId?.let { storyId ->
            db.collection("stories").document(storyId)
                .collection("chapters")
                .whereEqualTo("number", number)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshots ->
                    if (!snapshots.isEmpty) {
                        val doc = snapshots.documents[0]
                        val chapter = doc.toObject(Chapter::class.java)
                        chapter?.let {
                            it.id = doc.id
                            it.storyId = storyId
                            displayChapter(it)
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Không tìm thấy chương này", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.err_network), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadChapterById(chapterId: String) {
        binding.progressBar.visibility = View.VISIBLE
        currentStoryId?.let { storyId ->
            db.collection("stories").document(storyId)
                .collection("chapters").document(chapterId)
                .get()
                .addOnSuccessListener { doc ->
                    val chapter = doc.toObject(Chapter::class.java)
                    chapter?.let {
                        it.id = doc.id
                        it.storyId = storyId
                        displayChapter(it)
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.err_network), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun displayChapter(chapter: Chapter) {
        binding.progressBar.visibility = View.GONE
        currentChapterNumber = chapter.number
        
        binding.toolbar.title = getString(R.string.chapter_title_format, chapter.number, chapter.title)
        binding.tvChapterTitle.text = getString(R.string.chapter_title_format, chapter.number, chapter.title)
        binding.tvChapterContent.text = chapter.content.ifEmpty { getString(R.string.no_content) }
        
        binding.nestedScrollView.smoothScrollTo(0, 0)
        
        currentStoryId?.let {
            saveLastReadChapter(it, chapter.id, chapter.number)
        }
        
        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        binding.btnPrevChapter.isEnabled = currentChapterNumber > 1
        // We could query if next chapter exists, but for now we just enable it
        binding.btnNextChapter.isEnabled = true 
    }

    private fun saveLastReadChapter(storyId: String, chapterId: String, chapterNumber: Int) {
        val prefs = getSharedPreferences("ReaderPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("last_chapter_id_$storyId", chapterId)
            putInt("last_chapter_number_$storyId", chapterNumber)
            apply()
        }
    }

    private fun showSettingsDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogReadingSettingsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.sliderTextSize.value = textSize
        dialogBinding.toggleFont.check(if (isSerif) R.id.btnFontSerif else R.id.btnFontSans)

        dialogBinding.sliderTextSize.addOnChangeListener { _, value, _ ->
            textSize = value
            applyReadingSettings()
            savePreferences()
        }

        dialogBinding.toggleFont.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isSerif = checkedId == R.id.btnFontSerif
                applyReadingSettings()
                savePreferences()
            }
        }

        dialog.show()
    }

    private fun applyReadingSettings() {
        binding.tvChapterContent.textSize = textSize
        binding.tvChapterContent.typeface = if (isSerif) Typeface.SERIF else Typeface.SANS_SERIF
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences("ReadingSettings", Context.MODE_PRIVATE)
        textSize = prefs.getFloat("text_size", 19f)
        isSerif = prefs.getBoolean("is_serif", false)
    }

    private fun savePreferences() {
        val prefs = getSharedPreferences("ReadingSettings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat("text_size", textSize)
            putBoolean("is_serif", isSerif)
            apply()
        }
    }
}
