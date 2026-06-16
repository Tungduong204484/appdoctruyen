package com.example.appctruyn;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appctruyn.databinding.ActivityCommentsBinding;
import com.example.appctruyn.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private ActivityCommentsBinding binding;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CommentAdapter adapter;
    private final List<Comment> commentList = new ArrayList<>();
    private String storyId;
    private int chapterNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyId = getIntent().getStringExtra("storyId");
        chapterNumber = getIntent().getIntExtra("chapterNumber", 0);

        if (storyId == null) {
            Toast.makeText(this, "Không tìm thấy ID truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        loadComments();
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        adapter = new CommentAdapter(commentList);
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> postComment());
    }

    private void loadComments() {
        db.collection("comments")
                .whereEqualTo("storyId", storyId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Listen failed.", error);
                        return;
                    }
                    if (value != null) {
                        commentList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Comment comment = doc.toObject(Comment.class);
                            comment.setId(doc.getId());
                            commentList.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void postComment() {
        String content = binding.etComment.getText().toString().trim();
        if (content.isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, getString(R.string.login_to_comment), Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = user.getDisplayName();
        if (userName == null || userName.isEmpty()) userName = user.getEmail();

        Comment comment = new Comment(user.getUid(), userName, content, storyId, chapterNumber);
        
        binding.btnSend.setEnabled(false);
        db.collection("comments").add(comment)
                .addOnSuccessListener(documentReference -> {
                    binding.etComment.setText("");
                    binding.btnSend.setEnabled(true);
                    Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.btnSend.setEnabled(true);
                    Log.e("FirestoreError", "Error adding document", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
