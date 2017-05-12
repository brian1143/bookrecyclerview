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
            view = bookLayoutManager.findRotatingView();
        }
        //Log.i(TAG, "find snap view, view = " + view);
        return view;
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        float distanceToFinalDegree = targetView.getRotationY();
        if (Math.abs(distanceToFinalDegree) < 90) {
            out[0] = (int) (layoutManager.getWidth() * distanceToFinalDegree / 180);
        } else if (distanceToFinalDegree == 90) {
            if (layoutManager instanceof BookLayoutManager) {
                BookLayoutManager bookLayoutManager = (BookLayoutManager) layoutManager;
                View rotatingView = bookLayoutManager.findRotatingView();
                if (rotatingView != null) {
                    distanceToFinalDegree = rotatingView.getRotationY() - (-90) + 90;
                } else {
                    distanceToFinalDegree = 90;
                }
                out[0] = (int) (layoutManager.getWidth() * distanceToFinalDegree / 180);
            }
        } else if (distanceToFinalDegree == -90) {
            if (layoutManager instanceof BookLayoutManager) {
                BookLayoutManager bookLayoutManager = (BookLayoutManager) layoutManager;
                View rotatingView = bookLayoutManager.findRotatingView();
                if (rotatingView != null) {
                    distanceToFinalDegree = -(90 - rotatingView.getRotationY() + 90);
                } else {
                    distanceToFinalDegree = -90;
                }
                out[0] = (int) (layoutManager.getWidth() * distanceToFinalDegree / 180);
            }
        }
        //Log.i(TAG, "calculate distance to final snap, target = " + targetView.getId() + ", rotation = " + targetView.getRotationY() + ", distance = [" + out[0] + ", " + out[1] + "]");
        return out;
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        //Log.i(TAG, "find target snap position, velocityX = " + velocityX + ", velocityY = " + velocityY);
        if (velocityX == 0) {
            return RecyclerView.NO_POSITION;
        }

        View view = null;
        if (layoutManager instanceof BookLayoutManager) {
            BookLayoutManager bookLayoutManager = (BookLayoutManager) layoutManager;
            if (velocityX > 0) {
                view = bookLayoutManager.findViewByPageId(BookLayoutManager.ID_PAGE_LEFT_TOP);
            } else {
                view = bookLayoutManager.findViewByPageId(BookLayoutManager.ID_PAGE_RIGHT_TOP);
            }
        }

        if (view == null) {
            return RecyclerView.NO_POSITION;
        }

        return layoutManager.getPosition(view);
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
