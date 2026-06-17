package com.example.appctruyn;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    public interface OnFilterApplyListener {
        void onFilterApply(Bundle filters);
    }

    private OnFilterApplyListener listener;

    public void setOnFilterApplyListener(OnFilterApplyListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.FilterBottomSheetDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ChipGroup cgSort = view.findViewById(R.id.cgSort);
        ChipGroup cgType = view.findViewById(R.id.cgType);
        ChipGroup cgGender = view.findViewById(R.id.cgGender);
        ChipGroup cgStatus = view.findViewById(R.id.cgStatus);
        ChipGroup cgAttribute = view.findViewById(R.id.cgAttribute);
        ChipGroup cgChapters = view.findViewById(R.id.cgChapters);
        ChipGroup cgPublishDate = view.findViewById(R.id.cgPublishDate);
        ChipGroup cgGenre = view.findViewById(R.id.cgGenre);
        ChipGroup cgGenreOther = view.findViewById(R.id.cgGenreOther);
        View btnApply = view.findViewById(R.id.btnApply);

        // Set default selections
        setDefault(cgSort);
        setDefault(cgType);
        setDefault(cgGender);
        setDefault(cgStatus);
        setDefault(cgAttribute);
        setDefault(cgChapters);
        setDefault(cgPublishDate);

        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                Bundle filters = new Bundle();
                filters.putString("sort", getSelectedText(cgSort));
                filters.putString("type", getSelectedText(cgType));
                filters.putString("gender", getSelectedText(cgGender));
                filters.putString("status", getSelectedText(cgStatus));
                filters.putString("attribute", getSelectedText(cgAttribute));
                filters.putString("chapters", getSelectedText(cgChapters));
                filters.putString("publishDate", getSelectedText(cgPublishDate));
                
                ArrayList<String> genres = getSelectedList(cgGenre);
                genres.addAll(getSelectedList(cgGenreOther));
                filters.putStringArrayList("genres", genres);
                
                listener.onFilterApply(filters);
            }
            dismiss();
        });
    }

    private void setDefault(ChipGroup cg) {
        if (cg != null && cg.getCheckedChipId() == View.NO_ID && cg.getChildCount() > 0) {
            View first = cg.getChildAt(0);
            if (first instanceof Chip) {
                ((Chip) first).setChecked(true);
            }
        }
    }

    private String getSelectedText(ChipGroup cg) {
        if (cg == null) return "";
        int id = cg.getCheckedChipId();
        if (id != View.NO_ID) {
            Chip chip = cg.findViewById(id);
            if (chip != null) return chip.getText().toString();
        }
        return "";
    }

    private ArrayList<String> getSelectedList(ChipGroup cg) {
        ArrayList<String> list = new ArrayList<>();
        if (cg == null) return list;
        for (int id : cg.getCheckedChipIds()) {
            Chip chip = cg.findViewById(id);
            if (chip != null) list.add(chip.getText().toString());
        }
        return list;
    }
}
