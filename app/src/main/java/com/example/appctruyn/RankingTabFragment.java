package com.example.appctruyn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appctruyn.model.Comment;
import com.example.appctruyn.model.Story;
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
        
        fetchData();
        return view;
    }

    private void fetchData() {
        if ("Bình luận".equals(rankingType)) {
            fetchLatestComments();
        } else {
            fetchRankingStories();
        }
    }

    private void fetchLatestComments() {
        // Lấy 20 bình luận mới nhất trên toàn hệ thống
        db.collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && isAdded()) {
                        List<Comment> comments = task.getResult().toObjects(Comment.class);
                        CommentAdapter adapter = new CommentAdapter(comments);
                        // Click vào bình luận sẽ chuyển đến chi tiết truyện
                        adapter.setOnItemClickListener(comment -> navigateToStoryDetail(comment.getStoryId()));
                        rvRanking.setAdapter(adapter);
                    }
                });
    }

    private void fetchRankingStories() {
        Query query;
        switch (rankingType) {
            case "Đề cử":
                query = db.collection("stories").orderBy("rating", Query.Direction.DESCENDING);
                break;
            case "Lượt đọc":
                query = db.collection("stories").orderBy("views", Query.Direction.DESCENDING);
                break;
            default:
                query = db.collection("stories").orderBy("rating", Query.Direction.DESCENDING);
                break;
        }

        query.limit(20).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                List<Story> list = task.getResult().toObjects(Story.class);
                if (isAdded()) {
                    rvRanking.setAdapter(new RankingAdapter(list, story -> navigateToStoryDetail(story.getId())));
                }
            } else {
                // Fallback
                db.collection("stories").limit(20).get().addOnSuccessListener(snapshots -> {
                    if (isAdded() && snapshots != null) {
                        List<Story> list = snapshots.toObjects(Story.class);
                        rvRanking.setAdapter(new RankingAdapter(list, story -> navigateToStoryDetail(story.getId())));
                    }
                });
            }
        });
    }

    private void navigateToStoryDetail(String storyId) {
        if (storyId != null && !storyId.isEmpty() && isAdded()) {
            Intent intent = new Intent(requireContext(), StoryDetailActivity.class);
            intent.putExtra("storyId", storyId);
            startActivity(intent);
        }
    }
}
