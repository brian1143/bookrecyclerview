package com.brian.android.myapplication;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

class BookLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private static final String TAG = "BookLayoutManager";

    private View pageLeft, pageLeftBottom, pageLeftTop;
    private View pageRight, pageRightBottom, pageRightTop;
    private int currentPosition;
    private int scrollX;

    View findRotatingView() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            float rotation = view.getRotationY();
            if (Math.abs(rotation) % 90 != 0) {
                return view;
            }
        }
        return null;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return dx;
        }

        // Update scroll position.
        boolean forward = scrollX > 0 || (scrollX == 0 && dx > 0);
        if (forward) {
            if (scrollX + dx > getWidth()) {
                dx = getWidth() - scrollX;
                scrollX = getWidth();
            } else if (scrollX + dx < 0) {
                dx = -scrollX;
                scrollX = 0;
            } else {
                scrollX = scrollX + dx;
            }
        } else {
            if (scrollX + dx < -getWidth()) {
                dx = -getWidth() - scrollX;
                scrollX = -getWidth();
            } else if (scrollX + dx > 0) {
                dx = -scrollX;
                scrollX = 0;
            } else {
                scrollX = scrollX + dx;
            }
        }

        rotateViewByScroll();

        checkScrollEnd(recycler, state);

        return dx;
    }

    private void rotateViewByScroll() {
        float rotation;
        if (pageLeftBottom != null) {
            pageLeftBottom.setRotationY(0);
        }
        if (pageRightBottom != null) {
            pageRightBottom.setRotationY(0);
        }
        if (scrollX > 0) {
            if (pageLeft != null) {
                pageLeft.setRotationY(0);
            }
            if (pageRightTop != null) {
                pageRightTop.setRotationY(-90);
            }
            if (scrollX < getWidth() / 2) {
                rotation = -(180 * scrollX / getWidth());
                if (pageRight != null) {
                    pageRight.setRotationY(rotation);
                }
                if (pageLeftTop != null) {
                    pageLeftTop.setRotationY(90);
                }
            } else {
                if (pageRight != null) {
                    pageRight.setRotationY(-90);
                }
                rotation = 90 - (180 * (scrollX - getWidth() / 2) / getWidth());
                if (pageLeftTop != null) {
                    pageLeftTop.setRotationY(rotation);
                }
            }
        } else if (scrollX < 0) {
            if (pageRight != null) {
                pageRight.setRotationY(0);
            }
            if (pageLeftTop != null) {
                pageLeftTop.setRotationY(90);
            }
            if (scrollX > -getWidth() / 2) {
                rotation = -(180 * scrollX / getWidth());
                if (pageLeft != null) {
                    pageLeft.setRotationY(rotation);
                }
                if (pageRightTop != null) {
                    pageRightTop.setRotationY(-90);
                }
            } else {
                if (pageLeft != null) {
                    pageLeft.setRotationY(90);
                }
                rotation = -(180 * scrollX / getWidth()) - 180;
                if (pageRightTop != null) {
                    pageRightTop.setRotationY(rotation);
                }
            }
        } else {
            if (pageLeft != null) {
                pageLeft.setRotationY(0);
            }
            if (pageLeftTop != null) {
                pageLeftTop.setRotationY(90);
            }
            if (pageRight != null) {
                pageRight.setRotationY(0);
            }
            if (pageRightTop != null) {
                pageRightTop.setRotationY(-90);
            }
        }
        //Log.i(TAG, "rotate view by scroll, scroll = " + scrollX + ", rotation = " + rotation);
    }

    private void checkScrollEnd(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (scrollX == getWidth()) {
            if (pageLeftTop != null) {
                currentPosition = getPosition(pageLeftTop);
                fillPages(recycler, state);
                scrollX = 0;
                rotateViewByScroll();
            }
        }
        if (scrollX == -getWidth()) {
            if (pageLeftBottom != null) {
                currentPosition = getPosition(pageLeftBottom);
                fillPages(recycler, state);
                scrollX = 0;
                rotateViewByScroll();
            }
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        SparseArray<View> viewCache = new SparseArray<>(getChildCount());
        if (getChildCount() != 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                viewCache.put(getPosition(child), child);
            }
        }

        findCurrentPosition(viewCache, recycler, state);

        fillPages(recycler, state);

        rotateViewByScroll();
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0 || scrollX == 0) {
            return null;
        }
        int direction = scrollX > 0 ? 1 : -1;
        return new PointF(direction, 0);
    }

    private void fillPages(RecyclerView.Recycler recycler, RecyclerView.State state) {
        SparseArray<View> viewCache = new SparseArray<>(getChildCount());
        if (getChildCount() != 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                viewCache.put(getPosition(child), child);
            }
        }

        for (int i = 0; i < viewCache.size(); i++) {
            detachView(viewCache.valueAt(i));
        }

        pageLeftBottom = fillLeftPage(currentPosition - 2, viewCache, recycler, state);
        pageLeft = fillLeftPage(currentPosition, viewCache, recycler, state);
        pageLeftTop = fillLeftPage(currentPosition + 2, viewCache, recycler, state);
        pageRightBottom = fillRightPage(currentPosition + 3, viewCache, recycler, state);
        pageRight = fillRightPage(currentPosition + 1, viewCache, recycler, state);
        pageRightTop = fillRightPage(currentPosition - 1, viewCache, recycler, state);

        for (int i=0; i < viewCache.size(); i++) {
            recycler.recycleView(viewCache.valueAt(i));
        }
    }

    private void findCurrentPosition(SparseArray<View> viewCache, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int itemCount = state.getItemCount();
        int viewCount = viewCache.size();
        if (itemCount == 1) {
            if (viewCount == 0) {
                currentPosition = 0;
            } else {
                currentPosition = viewCache.keyAt(0);
            }
        } else if (itemCount == 2) {
            if (viewCount == 0) {
                currentPosition = 0;
            } else if (viewCount == 1) {
                currentPosition = viewCache.keyAt(0);
            } else {
                currentPosition = viewCache.keyAt(0);
            }
        } else if (itemCount == 3) {
            if (viewCount == 0) {
                currentPosition = 0;
            } else if (viewCount == 1) {
                currentPosition = viewCache.keyAt(0);
            } else if (viewCount == 2) {
                currentPosition = viewCache.keyAt(0);
            } else {
                currentPosition = getPosition(pageLeft);
            }
        } else if (itemCount == 4) {
            if (viewCount == 0) {
                currentPosition = 0;
            } else if (viewCount == 1) {
                currentPosition = viewCache.keyAt(0);
            } else if (viewCount == 2) {
                currentPosition = viewCache.keyAt(0);
            } else  {
                currentPosition = getPosition(pageLeft);
            }
        } else if (itemCount == 5) {
            if (viewCount == 0) {
                currentPosition = 0;
            } else if (viewCount == 1) {
                currentPosition = viewCache.keyAt(0);
            } else if (viewCount == 2) {
                currentPosition = viewCache.keyAt(0);
            } else {
                currentPosition = getPosition(pageLeft);
            }
        } else {
            if (viewCount == 0) {
                currentPosition = 0;
            } else if (viewCount == 1) {
                currentPosition = viewCache.keyAt(0);
            } else if (viewCount == 2) {
                currentPosition = viewCache.keyAt(0);
            } else {
                currentPosition = getPosition(pageLeft);
            }
        }
        Log.i(TAG, "find current position, item count = " + itemCount + ", view count = " + viewCount + ", position = " + currentPosition);
    }

    private View fillLeftPage(int position, SparseArray viewCache, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (position < 0 || position > state.getItemCount() - 1) {
            return null;
        }
        View view = (View) viewCache.get(position);
        if (view == null) {
            view = recycler.getViewForPosition(position);
            addView(view);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            Rect decorRect = new Rect();
            calculateItemDecorationsForChild(view, decorRect);
            int size = Math.max(0, getWidth() / 2 - getPaddingLeft() + lp.leftMargin + lp.rightMargin + decorRect.left + decorRect.right);
            int widthSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);
            int heightSpec = getChildMeasureSpec(
                    getHeight(),
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + decorRect.top + decorRect.bottom,
                    lp.height,
                    canScrollVertically()
            );
            view.measure(widthSpec, heightSpec);
            layoutDecorated(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        } else {
            attachView(view);
            viewCache.remove(position);
        }

        view.setPivotX(view.getWidth());
        view.setPivotY(view.getHeight() / 2);
        view.setCameraDistance(10 * view.getWidth());
        return view;
    }

    private View fillRightPage(int position, SparseArray viewCache, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (position < 0 || position > state.getItemCount() - 1) {
            return null;
        }
        View view = (View) viewCache.get(position);
        if (view == null) {
            view = recycler.getViewForPosition(position);
            addView(view);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            Rect decorRect = new Rect();
            calculateItemDecorationsForChild(view, decorRect);
            int size = Math.max(0, getWidth() / 2 - getPaddingRight() + lp.leftMargin + lp.rightMargin + decorRect.left + decorRect.right);
            int widthSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);
            int heightSpec = getChildMeasureSpec(
                    getHeight(),
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + decorRect.top + decorRect.bottom,
                    lp.height,
                    canScrollVertically()
            );
            view.measure(widthSpec, heightSpec);
            layoutDecorated(view, getWidth() / 2, 0, getWidth() / 2 + view.getMeasuredWidth(), view.getMeasuredHeight());
        } else {
            attachView(view);
            viewCache.remove(position);
        }

        view.setPivotX(0);
        view.setPivotY(view.getHeight() / 2);
        view.setCameraDistance(10 * view.getWidth());
        return view;
    }
}
