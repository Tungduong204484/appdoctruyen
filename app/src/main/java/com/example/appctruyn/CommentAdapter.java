package com.example.appctruyn;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appctruyn.databinding.ItemCommentBinding;
import com.example.appctruyn.model.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(commentList.get(position));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;

        public CommentViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Comment comment) {
            binding.tvUserName.setText(comment.getUserName());
            binding.tvContent.setText(comment.getContent());
            
            StringBuilder info = new StringBuilder();
            
            // Hiển thị chương nếu bình luận thuộc về 1 chương
            if (comment.getChapterNumber() > 0) {
                info.append("Chương ").append(comment.getChapterNumber()).append(" • ");
            }
            
            if (comment.getTimestamp() != null) {
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                        comment.getTimestamp().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS);
                info.append(relativeTime);
            }
            
            binding.tvTime.setText(info.toString());
        }
    }
}
