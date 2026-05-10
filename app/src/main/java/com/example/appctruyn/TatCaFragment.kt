package com.example.appctruyn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.appctruyn.databinding.FragmentTatCaBinding
import com.example.appctruyn.model.Story
import com.google.firebase.firestore.FirebaseFirestore

class TatCaFragment : Fragment() {

    private var _binding: FragmentTatCaBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTatCaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupClickListeners()
        fetchData()
    }

    private fun setupRecyclerViews() {
        // Đề cử - Grid 3 cột
        binding.rvDeCu.layoutManager = GridLayoutManager(requireContext(), 3)

        // Mới đăng - Ngang
        binding.rvMoiDang.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Mới hoàn thành - Grid 2 cột
        binding.rvMoiHoanThanh.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun setupClickListeners() {
        // Click header Đề cử
        binding.layoutHeaderDeCu.setOnClickListener {
            (activity as? MainActivity)?.switchToRanking("Đề cử")
        }
    }

    private fun fetchData() {
        // Banner - lấy truyện hot
        db.collection("stories")
            .whereEqualTo("isHot", true)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.toObjects(Story::class.java)
                android.util.Log.d("TatCaFragment", "Banner - Số lượng: ${list.size}")
                if (list.isNotEmpty()) {
                    setupBanner(list)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi tải banner: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Đề cử - lấy 6 truyện
        db.collection("stories")
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                val stories = documents.toObjects(Story::class.java)
                android.util.Log.d("TatCaFragment", "Đề cử - Số lượng: ${stories.size}")
                if (stories.isNotEmpty()) {
                    binding.rvDeCu.adapter = DeCuAdapter(stories) { story ->
                        navigateToStoryDetail(story.id)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi tải đề cử: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Mới đăng - lấy 10 truyện (BỎ orderBy)
        db.collection("stories")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val stories = documents.toObjects(Story::class.java)
                android.util.Log.d("TatCaFragment", "Mới đăng - Số lượng: ${stories.size}")
                stories.forEach { story ->
                    android.util.Log.d("TatCaFragment", "Truyện mới đăng: ${story.title}")
                }

                if (stories.isNotEmpty()) {
                    binding.rvMoiDang.adapter = MoiDangAdapter(stories) { story ->
                        navigateToStoryDetail(story.id)
                    }
                } else {
                    Toast.makeText(requireContext(), "Không có truyện mới đăng", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("TatCaFragment", "Lỗi mới đăng: ${e.message}")
                Toast.makeText(requireContext(), "Lỗi tải mới đăng: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Mới hoàn thành - lấy truyện có status = "Full" hoặc "Hoàn thành"
        db.collection("stories")
            .whereEqualTo("status", "Full")
            .limit(4)
            .get()
            .addOnSuccessListener { documents ->
                var list = documents.toObjects(Story::class.java)
                android.util.Log.d("TatCaFragment", "Mới hoàn thành (Full) - Số lượng: ${list.size}")

                if (list.isEmpty()) {
                    // Thử với status "Hoàn thành"
                    db.collection("stories")
                        .whereEqualTo("status", "Hoàn thành")
                        .limit(4)
                        .get()
                        .addOnSuccessListener { docs ->
                            list = docs.toObjects(Story::class.java)
                            android.util.Log.d("TatCaFragment", "Mới hoàn thành (Hoàn thành) - Số lượng: ${list.size}")

                            if (list.isNotEmpty()) {
                                binding.rvMoiHoanThanh.adapter = MoiHoanThanhAdapter(list) { story ->
                                    navigateToStoryDetail(story.id)
                                }
                            } else {
                                // Fallback: lấy truyện bất kỳ
                                db.collection("stories").limit(4).get().addOnSuccessListener { docs2 ->
                                    val stories = docs2.toObjects(Story::class.java)
                                    android.util.Log.d("TatCaFragment", "Fallback - Số lượng: ${stories.size}")
                                    if (stories.isNotEmpty()) {
                                        binding.rvMoiHoanThanh.adapter = MoiHoanThanhAdapter(stories) { story ->
                                            navigateToStoryDetail(story.id)
                                        }
                                    }
                                }
                            }
                        }
                } else {
                    binding.rvMoiHoanThanh.adapter = MoiHoanThanhAdapter(list) { story ->
                        navigateToStoryDetail(story.id)
                    }
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("TatCaFragment", "Lỗi hoàn thành: ${e.message}")
                Toast.makeText(requireContext(), "Lỗi tải hoàn thành: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBanner(list: List<Story>) {
        val adapter = BannerAdapter(list) { story ->
            navigateToStoryDetail(story.id)
        }
        binding.viewPagerBanner.adapter = adapter

        if (list.isNotEmpty()) {
            setupIndicators(list.size)
            binding.viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIndicators(position)
                }
            })
            startAutoSlide(list.size)
        }
    }

    private fun setupIndicators(size: Int) {
        binding.layoutIndicator.removeAllViews()
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }
        for (i in 0 until size) {
            val indicator = ImageView(requireContext()).apply {
                setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive))
                layoutParams = params
            }
            binding.layoutIndicator.addView(indicator)
        }
    }

    private fun updateIndicators(position: Int) {
        val childCount = binding.layoutIndicator.childCount
        for (i in 0 until childCount) {
            val imageView = binding.layoutIndicator.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active))
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive))
            }
        }
    }

    private fun startAutoSlide(size: Int) {
        bannerRunnable?.let { handler.removeCallbacks(it) }
        bannerRunnable = Runnable {
            var currentItem = binding.viewPagerBanner.currentItem
            currentItem = (currentItem + 1) % size
            binding.viewPagerBanner.currentItem = currentItem
            handler.postDelayed(bannerRunnable!!, 3000)
        }
        handler.postDelayed(bannerRunnable!!, 3000)
    }

    private fun navigateToStoryDetail(storyId: String?) {
        if (storyId != null && storyId.isNotEmpty()) {
            val intent = Intent(requireContext(), StoryDetailActivity::class.java)
            intent.putExtra("storyId", storyId)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy truyện", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerRunnable?.let { handler.removeCallbacks(it) }
        _binding = null
    }
}