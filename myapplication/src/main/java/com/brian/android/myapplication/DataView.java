package com.brian.android.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DataView extends ViewGroup {
    private static final String TAG = "DataView";
    private static final int[] COLOR_LIST = {Color.CYAN, Color.BLUE, Color.GREEN, Color.GRAY, Color.MAGENTA, Color.RED};
    private TextView textView;
    private MainActivity.AdapterData data;

    public DataView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        textView = new TextView(context);
        textView.setTextSize(128);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(textView);
    }

    void bindData(MainActivity.AdapterData data) {
        this.data = data;
        textView.setText(String.valueOf(data.id));
        int color = COLOR_LIST[Math.abs(data.id + 3) % COLOR_LIST.length];
        setBackgroundColor(color);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "on touch event, event = " + event.getAction() + ", data = " + data.id + ", rotation y = " + getRotationY());
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childLeft = (getWidth() - child.getMeasuredWidth()) / 2;
                int childTop = (getHeight() - child.getMeasuredHeight()) / 2;
                child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            }
        }
    }
}
