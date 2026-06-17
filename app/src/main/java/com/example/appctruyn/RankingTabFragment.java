package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appctruyn.model.Story;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class RankingTabFragment extends Fragment {

    private RecyclerView rvRanking;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String rankingType = "";

    public static RankingTabFragment newInstance(String type) {
        RankingTabFragment fragment = new RankingTabFragment();
        Bundle args = new Bundle();
        args.putString("RANKING_TYPE", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rankingType = getArguments().getString("RANKING_TYPE", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking_tab, container, false);
        rvRanking = view.findViewById(R.id.rvRanking);
        rvRanking.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Gọi hàm reset lượt đọc 1 lần để áp dụng quy tắc tính mới (mỗi tài khoản 1 lần)
        // Sau khi app chạy và báo "Đã reset...", bạn hãy comment dòng này lại.
        //resetAllViewsForTesting();
        
        fetchRankingData();
        
        return view;
    }

    private void fetchRankingData() {
        Query query;
        switch (rankingType) {
            case "Lượt đọc":
                query = db.collection("stories").orderBy("views", Query.Direction.DESCENDING);
                break;
            case "Đề cử":
                query = db.collection("stories")
                        .whereEqualTo("isHot", true)
                        .orderBy("rating", Query.Direction.DESCENDING);
                break;
            default:
                query = db.collection("stories").orderBy("rating", Query.Direction.DESCENDING);
                break;
        }

        // Dùng addSnapshotListener để tự động cập nhật thứ tự khi data thay đổi
        query.limit(20).addSnapshotListener((value, error) -> {
            if (error != null) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (value != null) {
                List<Story> list = value.toObjects(Story.class);
                rvRanking.setAdapter(new RankingAdapter(list, story -> navigateToStoryDetail(story.getId())));
            }
        });
    }

    /**
     * Hàm hỗ trợ Reset toàn bộ lượt đọc về 0 và xóa sạch sub-collection readers 
     * để test quy tắc "Mỗi tài khoản chỉ tính 1 lần".
     */
    private void resetAllViewsForTesting() {
        db.collection("stories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot storyDoc : queryDocumentSnapshots) {
                // Đưa views về 0
                storyDoc.getReference().update("views", 0);
                
                // Xóa dữ liệu readers cũ để tính lại từ đầu
                storyDoc.getReference().collection("readers").get().addOnSuccessListener(readers -> {
                    for (DocumentSnapshot readerDoc : readers) {
                        readerDoc.getReference().delete();
                    }
                });
            }
            if (isAdded()) {
                Toast.makeText(getContext(), "Đã Reset toàn bộ lượt đọc để tính lại từ đầu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToStoryDetail(String storyId) {
        if (storyId != null && !storyId.isEmpty()) {
            Intent intent = new Intent(requireContext(), StoryDetailActivity.class);
            intent.putExtra("storyId", storyId);
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), getString(R.string.story_not_found), Toast.LENGTH_SHORT).show();
        }
    }
}
