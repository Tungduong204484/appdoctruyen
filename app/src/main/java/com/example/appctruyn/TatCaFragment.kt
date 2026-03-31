package com.example.appctruyn

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.appctruyn.model.Story
import com.google.firebase.firestore.FirebaseFirestore

class TatCaFragment : Fragment() {

    private lateinit var viewPagerBanner: ViewPager2
    private lateinit var layoutIndicator: LinearLayout
    private lateinit var rvDeCu: RecyclerView
    private lateinit var rvMoiDang: RecyclerView
    private lateinit var rvMoiHoanThanh: RecyclerView
    private lateinit var layoutHeaderDeCu: RelativeLayout

    private val db = FirebaseFirestore.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tat_ca, container, false)

        viewPagerBanner = view.findViewById(R.id.viewPagerBanner)
        layoutIndicator = view.findViewById(R.id.layoutIndicator)
        rvDeCu = view.findViewById(R.id.rvDeCu)
        rvMoiDang = view.findViewById(R.id.rvMoiDang)
        rvMoiHoanThanh = view.findViewById(R.id.rvMoiHoanThanh)
        layoutHeaderDeCu = view.findViewById(R.id.layoutHeaderDeCu)

        setupRecyclerViews()
        fetchData()

        layoutHeaderDeCu.setOnClickListener {
            (activity as? MainActivity)?.switchToRanking("Đề cử")
        }

        return view
    }

    private fun setupRecyclerViews() {
        rvDeCu.layoutManager = GridLayoutManager(context, 3)
        rvMoiDang.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvMoiHoanThanh.layoutManager = GridLayoutManager(context, 2)
    }

    private fun fetchData() {
        db.collection("stories")
            .whereEqualTo("isHot", true)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.toObjects(Story::class.java)
                if (list.isNotEmpty()) {
                    setupBanner(list)
                }
            }

        db.collection("stories")
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                rvDeCu.adapter = DeCuAdapter(documents.toObjects(Story::class.java))
            }

        db.collection("stories")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                rvMoiDang.adapter = MoiDangAdapter(documents.toObjects(Story::class.java))
            }

        db.collection("stories")
            .whereEqualTo("status", "Full")
            .limit(4)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.toObjects(Story::class.java)
                if (list.isEmpty()) {
                    db.collection("stories").limit(4).get().addOnSuccessListener { docs ->
                        rvMoiHoanThanh.adapter = MoiHoanThanhAdapter(docs.toObjects(Story::class.java))
                    }
                } else {
                    rvMoiHoanThanh.adapter = MoiHoanThanhAdapter(list)
                }
            }
    }

    private fun setupBanner(list: List<Story>) {
        val adapter = BannerAdapter(list)
        viewPagerBanner.adapter = adapter
        setupIndicators(list.size)
        viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
            }
        })
        startAutoSlide(list.size)
    }

    private fun setupIndicators(size: Int) {
        layoutIndicator.removeAllViews()
        val indicators = arrayOfNulls<ImageView>(size)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }
        for (i in indicators.indices) {
            indicators[i] = ImageView(context)
            indicators[i]?.apply {
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.indicator_inactive))
                layoutParams = params
            }
            layoutIndicator.addView(indicators[i])
        }
    }

    private fun updateIndicators(position: Int) {
        val childCount = layoutIndicator.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicator.getChildAt(i) as ImageView
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
            var currentItem = viewPagerBanner.currentItem
            currentItem = (currentItem + 1) % size
            viewPagerBanner.currentItem = currentItem
            handler.postDelayed(bannerRunnable!!, 3000)
        }
        handler.postDelayed(bannerRunnable!!, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerRunnable?.let { handler.removeCallbacks(it) }
    }
}
