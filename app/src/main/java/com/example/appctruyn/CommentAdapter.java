package com.example.appctruyn;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appctruyn.databinding.ItemCommentBinding;
import com.example.appctruyn.model.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> commentList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Comment comment);
    }

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(commentList.get(position), listener);
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

        public void bind(Comment comment, OnItemClickListener listener) {
            binding.tvUserName.setText(comment.getUserName());
            binding.tvContent.setText(comment.getContent());
            
            if (comment.getTimestamp() != null) {
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                        comment.getTimestamp().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS);
                binding.tvTime.setText(relativeTime);
            } else {
                binding.tvTime.setText("");
            }
            
            // Hiển thị thông tin chương nếu có
            if (comment.getChapterNumber() > 0) {
                binding.tvChapterInfo.setVisibility(View.VISIBLE);
                binding.tvChapterInfo.setText("Chương " + comment.getChapterNumber());
            } else {
                binding.tvChapterInfo.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(comment);
                }
            });
        }
    }
}
