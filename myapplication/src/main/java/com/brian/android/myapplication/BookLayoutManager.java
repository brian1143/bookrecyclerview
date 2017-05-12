package com.brian.android.myapplication;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

class BookLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    @SuppressWarnings("unused")
    private static final String TAG = "BookLayoutManager";

    private View pageLeft, pageLeftBottom, pageLeftTop;
    private View pageRight, pageRightBottom, pageRightTop;
    private int pageLeftPosition;
    private int scrollX;

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

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        findCurrentPosition();

        fillPages(recycler, state);

        turnPageByScroll();
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return dx;
        }

        // Update scroll position.
        int fullTurnPageScrollDistance = 2 * getTurnPageScrollDistance();
        if (scrollX > 0) {
            // Scroll forward.
            if (scrollX + dx > fullTurnPageScrollDistance) {
                dx = fullTurnPageScrollDistance - scrollX;
                scrollX = fullTurnPageScrollDistance;
            } else if (scrollX + dx < 0) {
                dx = -scrollX;
                scrollX = 0;
            } else {
                scrollX += dx;
            }
        } else if (scrollX < 0) {
            // Scroll backward.
            if (scrollX + dx < -fullTurnPageScrollDistance) {
                dx = -fullTurnPageScrollDistance - scrollX;
                scrollX = -fullTurnPageScrollDistance;
            } else if (scrollX + dx > 0) {
                dx = -scrollX;
                scrollX = 0;
            } else {
                scrollX += dx;
            }
        } else {
            // Initial scroll.
            scrollX = dx;
            if (Math.abs(scrollX) > fullTurnPageScrollDistance) {
                scrollX = Integer.signum(dx) * fullTurnPageScrollDistance;
                dx = scrollX;
            }
        }

        turnPageByScroll();

        checkScrollEnd(recycler, state);

        return dx;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        //Log.i(TAG, "compute scroll vector for position, position = " + targetPosition);
        return null;
    }

    private void checkScrollEnd(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int fullTurnPageScrollDistance = 2 * getTurnPageScrollDistance();

        if (scrollX == fullTurnPageScrollDistance) {
            if (pageLeftTop != null) {
                pageLeftPosition = getPosition(pageLeftTop);
                fillPages(recycler, state);
                scrollX = 0;
                turnPageByScroll();
            }
        }

        if (scrollX == -fullTurnPageScrollDistance) {
            if (pageLeftBottom != null) {
                pageLeftPosition = getPosition(pageLeftBottom);
                fillPages(recycler, state);
                scrollX = 0;
                turnPageByScroll();
            }
        }
    }

    private void fillPages(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // Detach all current views and cache for reference.
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

        // According to page position, attach views.
        pageLeftBottom = attachLeftPage(pageLeftPosition - 2, viewCache, recycler, state);
        pageLeft = attachLeftPage(pageLeftPosition, viewCache, recycler, state);
        pageLeftTop = attachLeftPage(pageLeftPosition + 2, viewCache, recycler, state);
        pageRightBottom = attachRightPage(pageLeftPosition + 3, viewCache, recycler, state);
        pageRight = attachRightPage(pageLeftPosition + 1, viewCache, recycler, state);
        pageRightTop = attachRightPage(pageLeftPosition - 1, viewCache, recycler, state);

        // Recycler useless views.
        for (int i=0; i < viewCache.size(); i++) {
            recycler.recycleView(viewCache.valueAt(i));
        }
    }

    private void findCurrentPosition() {
        if (pageLeft != null) {
            pageLeftPosition = getPosition(pageLeft);
            return;
        }

        if (pageRight != null) {
            pageLeftPosition = getPosition(pageRight) - 1;
            return;
        }

        if (pageRightTop != null) {
            pageLeftPosition = getPosition(pageRightTop) + 1;
            return;
        }

        if (pageLeftTop != null) {
            pageLeftPosition = getPosition(pageLeftTop) - 2;
            return;
        }

        if (pageLeftBottom != null) {
            pageLeftPosition = getPosition(pageLeftBottom) + 2;
            return;
        }

        if (pageRightBottom != null) {
            pageLeftPosition = getPosition(pageRightBottom) - 3;
        }
        //Log.i(TAG, "find current position, position = " + pageLeftPosition);
    }

    private View attachLeftPage(int position, SparseArray<View> viewCache, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (position < 0 || position > state.getItemCount() - 1) {
            return null;
        }

        View view = viewCache.get(position);
        if (view == null) {
            view = recycler.getViewForPosition(position);
            view.setCameraDistance(5 * getWidth());
            addView(view);

            // Measure and layout child.
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
        } else {
            attachView(view);
            viewCache.remove(position);
        }

        view.setPivotX(view.getWidth());
        view.setPivotY(view.getHeight() / 2);
        return view;
    }

    private View attachRightPage(int position, SparseArray<View> viewCache, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (position < 0 || position > state.getItemCount() - 1) {
            return null;
        }
        View view = viewCache.get(position);
        if (view == null) {
            view = recycler.getViewForPosition(position);
            view.setCameraDistance(5 * getWidth());
            addView(view);

            // Measure and layout child.
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
        } else {
            attachView(view);
            viewCache.remove(position);
        }

        view.setPivotX(0);
        view.setPivotY(view.getHeight() / 2);
        return view;
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
        //Log.i(TAG, "rotate view by scroll, scroll = " + scrollX + ", rotation = " + rotation);
    }
}
