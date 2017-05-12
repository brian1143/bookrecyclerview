package com.brian.android.myapplication;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.view.View;

class BookSnapHelper extends SnapHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "BookSnapHelper";
    private static final float MILLISECONDS_PER_INCH = 20f;

    private RecyclerView recyclerView;

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        View view = null;
        if (layoutManager instanceof BookLayoutManager) {
            BookLayoutManager bookLayoutManager = (BookLayoutManager) layoutManager;
            view = bookLayoutManager.findSnapView();
        }
        return view;
    }

    @NonNull
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        if (layoutManager instanceof BookLayoutManager) {
            BookLayoutManager bookLayoutManager = (BookLayoutManager) layoutManager;
            return bookLayoutManager.calculateDistanceToFinalSnap(targetView);
        }
        return new int[2];
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (layoutManager instanceof BookLayoutManager) {
            BookLayoutManager bookLayoutManager = (BookLayoutManager) layoutManager;
            return bookLayoutManager.findTargetSnapPosition(velocityX);
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    protected LinearSmoothScroller createSnapScroller(RecyclerView.LayoutManager layoutManager) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return null;
        }
        return new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(recyclerView.getLayoutManager(),
                        targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };
    }
}
