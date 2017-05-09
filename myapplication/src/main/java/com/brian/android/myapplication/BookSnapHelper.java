package com.brian.android.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;

class BookSnapHelper extends SnapHelper {
    private static final String TAG = "BookSnapHelper";
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        out[0] = (int) (layoutManager.getWidth() * (180 - targetView.getRotationY()) / 180);
        Log.i(TAG, "calculate distance to final snap, target = " + targetView + ", distance = [" + out[0] + ", " + out[1] + "]");
        return out;
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        View view = null;
        if (layoutManager instanceof BookLayoutManager) {
            BookLayoutManager bookLayoutManager = (BookLayoutManager) layoutManager;
            view = bookLayoutManager.findRotatingView();
        }
        Log.i(TAG, "find snap view, view = " + view);
        return view;
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        Log.i(TAG, "find target snap position, velocityX = " + velocityX + ", velocityY = " + velocityY);
        return 0;
    }
}
