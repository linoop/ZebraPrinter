package com.technowavegroup.printerlib;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class RecyclerViewDecoration extends RecyclerView.ItemDecoration {
    private final int space;

    public RecyclerViewDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent, @NonNull @NotNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0)
            outRect.top = space;
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;
    }
}


