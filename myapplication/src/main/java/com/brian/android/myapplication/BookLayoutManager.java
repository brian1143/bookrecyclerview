package com.brian.android.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

class BookLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    @SuppressWarnings("unused")
    private static final String TAG = "BookLayoutManager";

    private View pageLeft, pageLeftBottom, pageLeftTop;
    private View pageRight, pageRightBottom, pageRightTop;
    private int pageLeftPosition;
    private int pendingPageLeftPosition;
    private int scrollX;
    private View retainingPage;
    private int retainingPagePosition;

    void retainPage(int position, Rect retainingRect, long duration) {
        if (isLeftPage(position)) {
            retainingPage = pageLeft;
            pageLeft = null;
        } else {
            retainingPage = pageRight;
            pageRight = null;
        }
        if (retainingPage == null) {
            return;
        }

        retainingPagePosition = position;
        // Re-order, put the retaining page on the top.
        detachView(retainingPage);
        attachView(retainingPage);

        Rect local = new Rect();
        retainingPage.getLocalVisibleRect(local);
        Rect from = new Rect(local);
        ObjectAnimator anim1 = ObjectAnimator.ofObject(retainingPage, "clipBounds", new RectEvaluator(), from, retainingRect);
        anim1.setDuration(duration / 4);
        anim1.setInterpolator(new LinearInterpolator());
        anim1.start();
        Rect end = new Rect(retainingRect);
        end.left = end.right;
        ObjectAnimator anim2 = ObjectAnimator.ofObject (retainingPage, "clipBounds", new RectEvaluator(), retainingRect, end);
        anim2.setDuration(150);
        anim2.setStartDelay(duration);
        anim2.start();
        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                retainingPagePosition = RecyclerView.NO_POSITION;
                requestLayout();
            }
        });
    }

    @SuppressWarnings("unused")
    int getPageLeftPosition() {
        return pageLeftPosition;
    }

    /**
     * Find a visible view for snap helper to calculate snap distance.
     * Normally, it should be either the left page or the right page.
     * @return the visible view.
     */
    View findSnapView() {
        if (pageLeft != null) {
            return pageLeft;
        }
        if (pageRight != null) {
            return pageRight;
        }
        return null;
    }

    /**
     * Calculate distance for snap helper to snap.
     * @param targetView If the view is either pageLeft or pageRight, it is a normal snap.
     *                   If the view is either pageLeftTop or pageRightTop, it is a flip.
     * @return the distance to snap.
     */
    int[] calculateDistanceToFinalSnap(@NonNull View targetView) {
        int turnPageDistance  = getTurnPageScrollDistance();
        int[] out = new int[2];

        if (targetView == pageLeft || targetView == pageRight) {
            if (Math.abs(scrollX) < turnPageDistance) {
                out[0] = -scrollX;
            } else {
                out[0] = Integer.signum(scrollX) * (2 * turnPageDistance - Math.abs(scrollX));
            }
        }

        if (targetView == pageLeftTop) {
            if (scrollX >= 0) {
                out[0] = 2 * turnPageDistance - scrollX;
            } else {
                out[0] = -scrollX;
            }
        }

        if (targetView == pageRightTop) {
            if (scrollX <= 0) {
                out[0] = - 2 * turnPageDistance - scrollX;
            } else {
                out[0] = -scrollX;
            }
        }
        //Log.i(TAG, "calculate distance to final snap, target = " + targetView.getId() + ", rotation = " + targetView.getRotationY() + ", distance = [" + out[0] + ", " + out[1] + "]");

        return out;
    }

    /**
     * For snap helper, find item position according to the fling velocity.
     * @param velocityX Fling velocity. velocityX > 0 for fling forward.
     * @return position of pageLeftTop or pageRightTop if non-null. Otherwise,
     * return position of pageLeft or pageRight.
     */
    int findTargetSnapPosition(int velocityX) {
        if (velocityX != 0) {
            if (velocityX > 0) {
                if (pageLeftTop != null) {
                    return getPosition(pageLeftTop);
                }
                if (pageLeft != null) {
                    return getPosition(pageLeft);
                }
            }
            if (velocityX < 0) {
                if (pageRightTop != null) {
                    return getPosition(pageRightTop);
                }
                if (pageRight != null) {
                    return getPosition(pageRight);
                }
            }
        }
        return RecyclerView.NO_POSITION;
    }

    BookLayoutManager() {
        super();
        pendingPageLeftPosition = RecyclerView.NO_POSITION;
        retainingPagePosition = RecyclerView.NO_POSITION;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pageLeftPosition = findPageLeftPosition();

        fillPages(pageLeftPosition, recycler, state);

        turnPageByScroll();
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() <= 1) {
            return 0;
        }

        // Update scroll position.
        int fullTurnPageScrollDistance = 2 * getTurnPageScrollDistance();
        if (scrollX > 0) {
            // Scroll forward.
            if (scrollX + dx > fullTurnPageScrollDistance) {
                //dx = fullTurnPageScrollDistance - scrollX;
                scrollX = fullTurnPageScrollDistance;
            } else if (scrollX + dx < 0) {
                //dx = -scrollX;
                scrollX = 0;
            } else {
                scrollX += dx;
            }
        } else if (scrollX < 0) {
            // Scroll backward.
            if (scrollX + dx < -fullTurnPageScrollDistance) {
                //dx = -fullTurnPageScrollDistance - scrollX;
                scrollX = -fullTurnPageScrollDistance;
            } else if (scrollX + dx > 0) {
                //dx = -scrollX;
                scrollX = 0;
            } else {
                scrollX += dx;
            }
        } else {
            if (dx > 0 && pageLeftTop == null) {
                return 0;
            }
            if (dx < 0 && pageRightTop == null) {
                return 0;
            }

            // Initial scroll.
            scrollX = dx;
            if (Math.abs(scrollX) > fullTurnPageScrollDistance) {
                scrollX = Integer.signum(dx) * fullTurnPageScrollDistance;
                //dx = scrollX;
            }
        }

        checkScrollEnd(recycler, state);

        turnPageByScroll();

        return dx;
    }

    @Override
    public void scrollToPosition(int position) {
        if (position < 0 || position >= getItemCount()) {
            return;
        }
        if (position == pageLeftPosition || position == pageLeftPosition + 1) {
            return;
        }

        boolean isLeftPage = (position - pageLeftPosition) % 2 == 0;
        pendingPageLeftPosition = isLeftPage ? position : position - 1;
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        if (position < 0 || position >= getItemCount()) {
            return;
        }
        if (position == pageLeftPosition || position == pageLeftPosition + 1) {
            return;
        }

        boolean isLeftPage = (position - pageLeftPosition) % 2 == 0;
        int targetPosition = isLeftPage ? position : position - 1;

        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                boolean forward = getPosition(targetView) >= pageLeftPosition + 2;
                int fullTurnPageScrollDistance = 2 * getTurnPageScrollDistance();
                final int dx = forward ? fullTurnPageScrollDistance : -fullTurnPageScrollDistance;
                final int time = calculateTimeForDeceleration(Math.abs(dx));
                if (time > 0) {
                    action.update(dx, 0, time, mDecelerateInterpolator);
                }
            }
        };
        linearSmoothScroller.setTargetPosition(targetPosition);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int direction = targetPosition < pageLeftPosition ? -1 : 1;
        Log.i(TAG, "compute scroll vector for position, position = " + targetPosition + ", current position = " + pageLeftPosition);
        return new PointF(direction, 0);
    }

    private void checkScrollEnd(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int fullTurnPageScrollDistance = 2 * getTurnPageScrollDistance();

        if (scrollX == fullTurnPageScrollDistance) {
            fillPages(pageLeftPosition + 2, recycler, state);
            scrollX = 0;
        }

        if (scrollX == -fullTurnPageScrollDistance) {
            fillPages(pageLeftPosition - 2, recycler, state);
            scrollX = 0;
        }
    }

    private View addViewFromRecycler(int position, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (position < 0 || position > state.getItemCount() - 1) {
            return null;
        }
        View view = recycler.getViewForPosition(position);
        addView(view);
        return view;
    }

    private void layoutLeftPage(View view) {
        if (view == null) {
            return;
        }
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
        Rect decorRect = new Rect();
        calculateItemDecorationsForChild(view, decorRect);
        int size = Math.max(0, getWidth() / 2 - getPaddingLeft() + lp.leftMargin + lp.rightMargin + decorRect.left + decorRect.right);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);
        int heightSpec = getChildMeasureSpec(
                getHeight(), getHeightMode(),
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + decorRect.top + decorRect.bottom,
                lp.height,
                canScrollVertically()
        );
        view.measure(widthSpec, heightSpec);
        layoutDecorated(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setCameraDistance(5 * getWidth());
        view.setPivotX(view.getWidth());
        view.setPivotY(view.getHeight() / 2);
    }

    private void layoutRightPage(View view) {
        if (view == null) {
            return;
        }
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
        Rect decorRect = new Rect();
        calculateItemDecorationsForChild(view, decorRect);
        int size = Math.max(0, getWidth() / 2 - getPaddingRight() + lp.leftMargin + lp.rightMargin + decorRect.left + decorRect.right);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);
        int heightSpec = getChildMeasureSpec(
                getHeight(), getHeightMode(),
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + decorRect.top + decorRect.bottom,
                lp.height,
                canScrollVertically()
        );
        view.measure(widthSpec, heightSpec);
        layoutDecorated(view, getWidth() / 2, 0, getWidth() / 2 + view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setCameraDistance(5 * getWidth());
        view.setPivotX(0);
        view.setPivotY(view.getHeight() / 2);
    }

    private boolean isLeftPage(int position) {
        return (position - pageLeftPosition) % 2 == 0;
    }

    private void fillPages(int pageLeftPosition, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (retainingPage != null) {
            detachView(retainingPage);
        }

        // Cache all existing page view before detaching.
        List<View> cacheView = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i ++) {
            cacheView.add(getChildAt(i));
        }

        detachAndScrapAttachedViews(recycler);

        // According to page position, attach page views.
        pageLeftBottom = addViewFromRecycler(pageLeftPosition - 2, recycler, state);
        pageLeft = addViewFromRecycler(pageLeftPosition, recycler, state);
        pageLeftTop = addViewFromRecycler(pageLeftPosition + 2, recycler, state);
        pageRightBottom = addViewFromRecycler(pageLeftPosition + 3, recycler, state);
        pageRight = addViewFromRecycler(pageLeftPosition + 1, recycler, state);
        pageRightTop = addViewFromRecycler(pageLeftPosition - 1, recycler, state);
        this.pageLeftPosition = pageLeftPosition;

        // Recycle useless page views
        for (View view : cacheView) {
            boolean inUse = (view == pageLeftBottom || view == pageLeft || view == pageLeftTop
                    || view == pageRightBottom || view == pageRight || view == pageRightTop);
            if (!inUse) {
                recycler.recycleView(view);
            }
        }

        // Measure and layout pages
        layoutLeftPage(pageLeftBottom);
        layoutLeftPage(pageLeft);
        layoutLeftPage(pageLeftTop);
        layoutRightPage(pageRightBottom);
        layoutRightPage(pageRight);
        layoutRightPage(pageRightTop);

        // Attach the retaining page or recycle when the retaining time up.
        if (retainingPage != null) {
            if (retainingPagePosition == RecyclerView.NO_POSITION) {
                retainingPage.setClipBounds(null);
                recycler.recycleView(retainingPage);
                retainingPage = null;
            } else {
                attachView(retainingPage);
            }
        }
    }

    private int findPageLeftPosition() {
        if (pendingPageLeftPosition != RecyclerView.NO_POSITION) {
            int pageLeftPosition = pendingPageLeftPosition;
            pendingPageLeftPosition = RecyclerView.NO_POSITION;
            return pageLeftPosition;
        }

        if (pageLeft != null) {
            return getPosition(pageLeft);
        }

        if (pageRight != null) {
            return getPosition(pageRight) - 1;
        }

        if (pageRightTop != null) {
            return getPosition(pageRightTop) + 1;
        }

        if (pageLeftTop != null) {
            return getPosition(pageLeftTop) - 2;
        }

        if (pageLeftBottom != null) {
            return getPosition(pageLeftBottom) + 2;
        }

        if (pageRightBottom != null) {
            return getPosition(pageRightBottom) - 3;
        }

        return 0;
        //Log.i(TAG, "find current position, position = " + pageLeftPosition);
    }

    /**
     * Get the total scrolling distance for page to turn 90 degree.
     * @return the scroll distance.
     */
    private int getTurnPageScrollDistance() {
        return getWidth() / 2;
    }

    private void turnPageByScroll() {
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
            int turnPageScrollDistance = getTurnPageScrollDistance();
            if (scrollX < turnPageScrollDistance) {
                rotation = 90 * scrollX / turnPageScrollDistance;
                if (pageRight != null) {
                    pageRight.setRotationY(-rotation);
                }
                if (pageLeftTop != null) {
                    pageLeftTop.setRotationY(90);
                }
            } else {
                if (pageRight != null) {
                    pageRight.setRotationY(-90);
                }
                rotation = 90 * (2 * turnPageScrollDistance - scrollX) / turnPageScrollDistance;
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
            int turnPageScrollDistance = getTurnPageScrollDistance();
            if (scrollX > -turnPageScrollDistance) {
                rotation = 90 * -scrollX / turnPageScrollDistance;
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
                rotation = 90 * (2 * turnPageScrollDistance - (-scrollX)) / turnPageScrollDistance;
                if (pageRightTop != null) {
                    pageRightTop.setRotationY(-rotation);
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

        if (pageLeft != null) {
            pageLeft.setVisibility(pageLeft.getRotationY() == 90 ? View.INVISIBLE : View.VISIBLE);
        }
        if (pageLeftTop != null) {
            pageLeftTop.setVisibility(pageLeftTop.getRotationY() == 90 ? View.INVISIBLE : View.VISIBLE);
        }
        if (pageLeftBottom != null) {
            pageLeftBottom.setVisibility(View.VISIBLE);
        }
        if (pageRight != null) {
            pageRight.setVisibility(pageRight.getRotationY() == -90 ? View.INVISIBLE : View.VISIBLE);
        }
        if (pageRightTop != null) {
            pageRightTop.setVisibility(pageRightTop.getRotationY() == -90 ? View.INVISIBLE : View.VISIBLE);
        }
        if (pageRightBottom != null) {
            pageRightBottom.setVisibility(View.VISIBLE);
        }
        //Log.i(TAG, "rotate view by scroll, scroll = " + scrollX + ", rotation = " + rotation);
    }
}
