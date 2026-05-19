package com.example.appctruyn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.appctruyn.model.LibraryStory
import com.google.firebase.firestore.FirebaseFirestore

class LibraryFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var rvLibrary: RecyclerView
    private lateinit var tvEmpty: android.widget.TextView
    private val db = FirebaseFirestore.getInstance()
    private var libraryAdapter: LibraryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayoutLibrary)
        rvLibrary = view.findViewById(R.id.rvLibrary)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        rvLibrary.layoutManager = LinearLayoutManager(requireContext())

        setupTabs()
        loadHistory()
    }

    override fun onResume() {
        super.onResume()
        // Reload khi quay lại fragment để cập nhật lịch sử mới nhất
        val currentTab = tabLayout.selectedTabPosition
        if (currentTab == 0) loadHistory() else loadBookmarks()
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Lịch sử"))
        tabLayout.addTab(tabLayout.newTab().setText("Đánh dấu"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) loadHistory() else loadBookmarks()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadHistory() {
        val prefs = requireContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
        val historyJson = prefs.getString("history_list", "[]") ?: "[]"

        // Parse lịch sử từ SharedPreferences
        val historyItems = parseHistoryJson(historyJson)

        if (historyItems.isEmpty()) {
            showEmpty("Chưa có lịch sử đọc")
            return
        }

        tvEmpty.visibility = View.GONE
        rvLibrary.visibility = View.VISIBLE

        libraryAdapter = LibraryAdapter(
            historyItems,
            onItemClick = { item ->
                val intent = Intent(requireContext(), StoryDetailActivity::class.java)
                intent.putExtra("storyId", item.storyId)
                startActivity(intent)
            },
            onMenuClick = { item, anchor ->
                showStoryMenu(item)
            }
        )
        rvLibrary.adapter = libraryAdapter
    }

    private fun loadBookmarks() {
        val prefs = requireContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
        val bookmarkJson = prefs.getString("bookmark_list", "[]") ?: "[]"
        val bookmarks = parseHistoryJson(bookmarkJson)

        if (bookmarks.isEmpty()) {
            showEmpty("Chưa có truyện đánh dấu")
            return
        }

        tvEmpty.visibility = View.GONE
        rvLibrary.visibility = View.VISIBLE

        libraryAdapter = LibraryAdapter(
            bookmarks,
            onItemClick = { item ->
                val intent = Intent(requireContext(), StoryDetailActivity::class.java)
                intent.putExtra("storyId", item.storyId)
                startActivity(intent)
            },
            onMenuClick = { item, anchor ->
                showStoryMenu(item)
            }
        )
        rvLibrary.adapter = libraryAdapter
    }

    private fun showEmpty(msg: String) {
        tvEmpty.text = msg
        tvEmpty.visibility = View.VISIBLE
        rvLibrary.visibility = View.GONE
    }

    private fun showStoryMenu(item: LibraryStory) {
        val sheet = LibraryBottomSheet.newInstance(item)
        sheet.onDeleteClick = {
            removeFromHistory(item.storyId)
            loadHistory()
        }
        sheet.onBookmarkToggle = { isBookmarked ->
            if (isBookmarked) {
                addBookmark(item)
            } else {
                removeBookmark(item.storyId)
            }
        }
        sheet.show(childFragmentManager, "LibraryMenu")
    }

    private fun removeFromHistory(storyId: String) {
        val prefs = requireContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
        val list = parseHistoryJson(prefs.getString("history_list", "[]") ?: "[]").toMutableList()
        list.removeAll { it.storyId == storyId }
        prefs.edit().putString("history_list", toHistoryJson(list)).apply()
        Toast.makeText(requireContext(), "Đã xóa khỏi tủ truyện", Toast.LENGTH_SHORT).show()
    }

    private fun addBookmark(item: LibraryStory) {
        val prefs = requireContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
        val list = parseHistoryJson(prefs.getString("bookmark_list", "[]") ?: "[]").toMutableList()
        if (list.none { it.storyId == item.storyId }) {
            list.add(0, item)
            prefs.edit().putString("bookmark_list", toHistoryJson(list)).apply()
            Toast.makeText(requireContext(), "Đã thêm vào đánh dấu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeBookmark(storyId: String) {
        val prefs = requireContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
        val list = parseHistoryJson(prefs.getString("bookmark_list", "[]") ?: "[]").toMutableList()
        list.removeAll { it.storyId == storyId }
        prefs.edit().putString("bookmark_list", toHistoryJson(list)).apply()
    }

    // JSON helpers (đơn giản, không cần Gson)
    companion object {
        fun parseHistoryJson(json: String): List<LibraryStory> {
            return try {
                val result = mutableListOf<LibraryStory>()
                if (json == "[]" || json.isBlank()) return result
                val trimmed = json.trim().removePrefix("[").removeSuffix("]")
                val items = splitJsonObjects(trimmed)
                for (item in items) {
                    val storyId = extractField(item, "storyId")
                    val title = extractField(item, "title")
                    val coverUrl = extractField(item, "coverUrl")
                    val lastChap = extractField(item, "lastChap").toIntOrNull() ?: 0
                    val totalChap = extractField(item, "totalChap").toIntOrNull() ?: 0
                    val notifyEnabled = extractField(item, "notifyEnabled") == "true"
                    if (storyId.isNotEmpty()) {
                        result.add(LibraryStory(storyId, title, coverUrl, lastChap, totalChap, notifyEnabled))
                    }
                }
                result
            } catch (e: Exception) { emptyList() }
        }

        private fun extractField(json: String, key: String): String {
            val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\"|\"$key\"\\s*:\\s*([^,}]*)".toRegex()
            val match = pattern.find(json) ?: return ""
            return (match.groupValues[1].ifEmpty { match.groupValues[2] }).trim()
        }

        private fun splitJsonObjects(s: String): List<String> {
            val result = mutableListOf<String>()
            var depth = 0
            var start = -1
            for (i in s.indices) {
                when (s[i]) {
                    '{' -> { if (depth == 0) start = i; depth++ }
                    '}' -> { depth--; if (depth == 0 && start != -1) { result.add(s.substring(start, i + 1)); start = -1 } }
                }
            }
            return result
        }

        fun toHistoryJson(list: List<LibraryStory>): String {
            return "[" + list.joinToString(",") { item ->
                """{"storyId":"${item.storyId}","title":"${item.title}","coverUrl":"${item.coverUrl}","lastChap":${item.lastChap},"totalChap":${item.totalChap},"notifyEnabled":${item.notifyEnabled}}"""
            } + "]"
        }

        /**
         * Gọi từ ReadChapterActivity khi đọc xong để lưu vào tủ truyện
         */
        fun saveToHistory(
            context: Context,
            storyId: String,
            title: String,
            coverUrl: String,
            lastChap: Int,
            totalChap: Int
        ) {
            val prefs = context.getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
            val list = parseHistoryJson(prefs.getString("history_list", "[]") ?: "[]").toMutableList()

            // Xóa nếu đã có rồi (để đưa lên đầu)
            list.removeAll { it.storyId == storyId }

            // Thêm vào đầu danh sách
            list.add(0, LibraryStory(storyId, title, coverUrl, lastChap, totalChap))

            // Giới hạn 100 truyện
            val trimmed = if (list.size > 100) list.subList(0, 100) else list
            prefs.edit().putString("history_list", toHistoryJson(trimmed)).apply()
        }
    }
}