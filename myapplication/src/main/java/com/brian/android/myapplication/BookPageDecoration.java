package com.brian.android.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

class BookPageDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "BookPageDecoration";

    private int offsetTop, offsetBottom, offsetLeft, offsetRight;
    private Drawable leftPageCenterShadow, rightPageCenterShadow, leftPageSideShadow, rightPageSideShadow;

    BookPageDecoration(Context context) {
        leftPageCenterShadow = ContextCompat.getDrawable(context, R.drawable.left_page_center_shadow);
        rightPageCenterShadow = ContextCompat.getDrawable(context, R.drawable.right_page_center_shadow);
        leftPageSideShadow = ContextCompat.getDrawable(context, R.drawable.left_page_center_shadow);
        rightPageSideShadow = ContextCompat.getDrawable(context, R.drawable.right_page_center_shadow);
        offsetTop = 20;
        offsetBottom = 20;
        offsetLeft = leftPageSideShadow.getIntrinsicWidth();
        offsetRight = rightPageSideShadow.getIntrinsicWidth();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        BookLayoutManager layoutManager = (BookLayoutManager) parent.getLayoutManager();
        boolean showLeftShadow = (layoutManager.getPageLeftBottom() != null && layoutManager.getPageLeftBottom().getRotationY() == 0)
                || (layoutManager.getPageLeft() != null && layoutManager.getPageLeft().getRotationY() == 0);
        boolean showRightShadow = (layoutManager.getPageRightBottom() != null && layoutManager.getPageRightBottom().getRotationY() == 0)
                || (layoutManager.getPageRight() != null && layoutManager.getPageRight().getRotationY() == 0);
        if (showLeftShadow) {
            leftPageSideShadow.setBounds(0, offsetTop, offsetLeft, parent.getHeight() - offsetBottom);
            leftPageSideShadow.draw(c);
        }

        if (showRightShadow) {
            rightPageSideShadow.setBounds(parent.getWidth() - offsetRight, offsetTop, parent.getWidth(), parent.getHeight() - offsetBottom);
            rightPageSideShadow.draw(c);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        BookLayoutManager layoutManager = (BookLayoutManager) parent.getLayoutManager();
        boolean hideLeftShadow = layoutManager.getPageLeftBottom() == null
                && (layoutManager.getPageLeft() == null || layoutManager.getPageLeft().getVisibility() == View.INVISIBLE)
                && (layoutManager.getPageLeftTop() == null || layoutManager.getPageLeftTop().getVisibility() == View.INVISIBLE);
        boolean hideRightShadow = layoutManager.getPageRightBottom() == null
                && (layoutManager.getPageRight() == null || layoutManager.getPageRight().getVisibility() == View.INVISIBLE)
                && (layoutManager.getPageRightTop() == null || layoutManager.getPageRightTop().getVisibility() == View.INVISIBLE);

        if (!hideLeftShadow) {
            leftPageCenterShadow.setBounds(parent.getWidth() / 2 - leftPageCenterShadow.getIntrinsicWidth(), offsetTop, parent.getWidth() / 2, parent.getHeight() - offsetBottom);
            leftPageCenterShadow.draw(c);
        }

        if (!hideRightShadow) {
            rightPageCenterShadow.setBounds(parent.getWidth() / 2, offsetTop, parent.getWidth() / 2 + leftPageCenterShadow.getIntrinsicWidth(), parent.getHeight() - offsetBottom);
            rightPageCenterShadow.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        BookLayoutManager layoutManager = (BookLayoutManager) parent.getLayoutManager();
        if (layoutManager.isLeftPage(view)) {
            outRect.set(offsetLeft, offsetTop, 0, offsetBottom);
        } else {
            outRect.set(0, offsetTop, offsetRight, offsetBottom);
        }
    }
}
