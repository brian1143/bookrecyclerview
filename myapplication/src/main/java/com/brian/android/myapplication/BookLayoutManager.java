package com.brian.android.myapplication;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

class BookLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "BookLayoutManager";

    private static final int ID_PAGE_LEFT = 0;
    private static final int ID_PAGE_RIGHT = 1;
    private static final int ID_PAGE_LEFT_BOTTOM = 2;
    private static final int ID_PAGE_RIGHT_BOTTOM = 3;
    private static final int ID_PAGE_LEFT_TOP = 4;
    private static final int ID_PAGE_RIGHT_TOP = 5;

    private int currentPosition;
    private int scrollX;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return dx;
        }

        if (scrollX + dx > getWidth()) {
            dx = getWidth() - scrollX;
            scrollX = getWidth();
        } else if (scrollX + dx < -getWidth()) {
            dx = -getWidth() - scrollX;
            scrollX = -getWidth();
        } else {
            scrollX += dx;
        }
        Log.i(TAG, "scroll by, dx = " + dx + ", scroll x = " + scrollX);

        View rightView = findViewById(ID_PAGE_RIGHT);
        float rightViewRotation = -(180 * Math.min(getWidth() / 2, Math.max(0, scrollX)) / getWidth());
        rightView.setRotationY(rightViewRotation);

        View leftView = findViewById(ID_PAGE_LEFT);
        float leftViewRotation = -(180 * Math.min(0, Math.max(- getWidth() / 2, scrollX)) / getWidth());
        leftView.setRotationY(leftViewRotation);

        View topRightView = findViewById(ID_PAGE_RIGHT_TOP);
        float topRightViewRotation = -(180 * Math.min(- getWidth() / 2, Math.max(- getWidth(), scrollX)) / getWidth()) - 180;
        topRightView.setRotationY(topRightViewRotation);

        View topLeftView = findViewById(ID_PAGE_LEFT_TOP);
        float topLeftViewRotation = -(180 * Math.min(getWidth(), Math.max(getWidth() / 2, scrollX)) / getWidth()) - 180;;
        topLeftView.setRotationY(topLeftViewRotation);

        return dx;
    }

    private View findViewById(int pageId) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view.getId() == pageId) {
                return view;
            }
        }
        return null;
    }

    private View findRightView(RecyclerView.Recycler recycler, RecyclerView.State state) {
        View viewFound = null;
        int viewPosition = Integer.MAX_VALUE;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (isRightView(view) && view.getVisibility() == View.VISIBLE) {
                int position = getPosition(view);
                if (position < viewPosition) {
                    viewFound = view;
                    viewPosition = position;
                }
            }
        }
        return viewFound;
    }

    private View findLeftView(RecyclerView.Recycler recycler, RecyclerView.State state) {
        View viewFound = null;
        int viewPosition = Integer.MIN_VALUE;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (isLeftView(view) && view.getVisibility() == View.VISIBLE) {
                int position = getPosition(view);
                if (position > viewPosition) {
                    viewFound = view;
                    viewPosition = position;
                }
            }
        }
        return viewFound;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        SparseArray<View> viewCache = new SparseArray(getChildCount());
        if (getChildCount() != 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                viewCache.put(getPosition(child), child);
            }
        }

        findCurrentPosition(viewCache, recycler, state);

        for (int i = 0; i < viewCache.size(); i++) {
            detachView(viewCache.valueAt(i));
        }

        fillLeftPage(currentPosition - 2, viewCache, ID_PAGE_LEFT_BOTTOM, recycler, state);
        fillLeftPage(currentPosition, viewCache, ID_PAGE_LEFT, recycler, state);
        fillLeftPage(currentPosition + 2, viewCache, ID_PAGE_LEFT_TOP, recycler, state);
        fillRightPage(currentPosition + 3, viewCache, ID_PAGE_RIGHT_BOTTOM, recycler, state);
        fillRightPage(currentPosition + 1, viewCache, ID_PAGE_RIGHT, recycler, state);
        fillRightPage(currentPosition - 1, viewCache, ID_PAGE_RIGHT_TOP, recycler, state);

        for (int i=0; i < viewCache.size(); i++) {
            recycler.recycleView(viewCache.valueAt(i));
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
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
                currentPosition = getPosition(findViewById(ID_PAGE_LEFT));
            }
        } else if (itemCount == 4) {
            if (viewCount == 0) {
                currentPosition = 0;
            } else if (viewCount == 1) {
                currentPosition = viewCache.keyAt(0);
            } else if (viewCount == 2) {
                currentPosition = viewCache.keyAt(0);
            } else  {
                currentPosition = getPosition(findViewById(ID_PAGE_LEFT));
            }
        } else if (itemCount == 5) {
            if (viewCount == 0) {
                currentPosition = 0;
            } else if (viewCount == 1) {
                currentPosition = viewCache.keyAt(0);
            } else if (viewCount == 2) {
                currentPosition = viewCache.keyAt(0);
            } else {
                currentPosition = getPosition(findViewById(ID_PAGE_LEFT));
            }
        } else {
            if (viewCount == 0) {
                currentPosition = 0;
            } else if (viewCount == 1) {
                currentPosition = viewCache.keyAt(0);
            } else if (viewCount == 2) {
                currentPosition = viewCache.keyAt(0);
            } else {
                currentPosition = getPosition(findViewById(ID_PAGE_LEFT));
            }
        }
        Log.i(TAG, "find current position, item count = " + itemCount + ", view count = " + viewCount + ", position = " + currentPosition);
    }

    private int findLeftVisibleViewPosition(SparseArray<View> viewCache) {
        int result = -1;
        for (int i = 0; i < viewCache.size(); i++) {
            int position = viewCache.keyAt(i);
            View view = viewCache.get(position);
            if (isLeftView(view) && view.getVisibility() == View.VISIBLE) {
                result = Math.max(result, position);
            }
        }
        return result;
    }

    private boolean isLeftView(View view) {
        return view.getLeft() < getWidth() / 2;
    }

    private boolean isRightView(View view) {
        return view.getLeft() >= getWidth() / 2;
    }

    private void fillLeftPage(int position, SparseArray viewCache, int pageId, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (position < 0 || position > state.getItemCount() - 1) {
            return;
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

        view.setId(pageId);
        view.setPivotX(view.getWidth());
        if (pageId == ID_PAGE_LEFT_TOP) {
            view.setRotationY(90);
        }
    }

    private void fillRightPage(int position, SparseArray viewCache, int pageId, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (position < 0 || position > state.getItemCount() - 1) {
            return;
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

        view.setId(pageId);
        view.setPivotX(0);
        if (pageId == ID_PAGE_RIGHT_TOP) {
            view.setRotationY(-90);
        }
    }
}
