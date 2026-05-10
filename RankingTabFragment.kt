package com.example.appctruyn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appctruyn.model.Story
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RankingTabFragment : Fragment() {

    private lateinit var rvRanking: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private var rankingType: String = ""

    companion object {
        fun newInstance(type: String): RankingTabFragment {
            val fragment = RankingTabFragment()
            val args = Bundle()
            args.putString("RANKING_TYPE", type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rankingType = arguments?.getString("RANKING_TYPE") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking_tab, container, false)
        rvRanking = view.findViewById(R.id.rvRanking)
        rvRanking.layoutManager = LinearLayoutManager(context)
        
        fetchRankingData()
        
        return view
    }

    private fun fetchRankingData() {
        val query = when (rankingType) {
            "Lượt đọc" -> db.collection("stories").orderBy("views", Query.Direction.DESCENDING)
            "Đề cử" -> db.collection("stories").whereEqualTo("isHot", true)
            else -> db.collection("stories").limit(20)
        }

        query.limit(20).get().addOnSuccessListener { documents ->
            val list = documents.toObjects(Story::class.java)
            rvRanking.adapter = RankingAdapter(list)
        }
    }
}
