package com.example.appctruyn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.appctruyn.model.LibraryStory;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LibraryBottomSheet extends BottomSheetDialogFragment {

    public interface OnDeleteClickListener {
        void onDeleteClick();
    }

    public interface OnBookmarkToggleListener {
        void onBookmarkToggle(boolean isChecked);
    }

    private OnDeleteClickListener onDeleteClick;
    private OnBookmarkToggleListener onBookmarkToggle;

    private static final String ARG_STORY_ID = "storyId";
    private static final String ARG_TITLE = "title";
    private static final String ARG_COVER = "coverUrl";
    private static final String ARG_LAST_CHAP = "lastChap";
    private static final String ARG_TOTAL_CHAP = "totalChap";
    private static final String ARG_NOTIFY = "notifyEnabled";

    public static LibraryBottomSheet newInstance(LibraryStory item) {
        LibraryBottomSheet fragment = new LibraryBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_STORY_ID, item.getStoryId());
        args.putString(ARG_TITLE, item.getTitle());
        args.putString(ARG_COVER, item.getCoverUrl());
        args.putInt(ARG_LAST_CHAP, item.getLastChap());
        args.putInt(ARG_TOTAL_CHAP, item.getTotalChap());
        args.putBoolean(ARG_NOTIFY, item.isNotifyEnabled());
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClick = listener;
    }

    public void setOnBookmarkToggleListener(OnBookmarkToggleListener listener) {
        this.onBookmarkToggle = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : "";
        String coverUrl = getArguments() != null ? getArguments().getString(ARG_COVER) : "";
        boolean notifyEnabled = getArguments() != null && getArguments().getBoolean(ARG_NOTIFY);

        ImageView ivCover = view.findViewById(R.id.ivCover);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        View btnDownload = view.findViewById(R.id.btnDownload);
        View btnDelete = view.findViewById(R.id.btnDelete);
        Switch switchNotify = view.findViewById(R.id.switchNotify);

        tvTitle.setText(title);
        switchNotify.setChecked(notifyEnabled);

        Glide.with(requireContext()).load(coverUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivCover);

        btnDownload.setOnClickListener(v -> {
            // Tính năng tải truyện (sắp ra mắt)
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) {
                onDeleteClick.onDeleteClick();
            }
            dismiss();
        });

        switchNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onBookmarkToggle != null) {
                onBookmarkToggle.onBookmarkToggle(isChecked);
            }
        });
    }
}
