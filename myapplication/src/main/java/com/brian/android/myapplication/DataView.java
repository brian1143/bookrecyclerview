package com.brian.android.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DataView extends ViewGroup {
    private static final int[] COLOR_LIST = {Color.CYAN, Color.BLUE, Color.GREEN, Color.GRAY};
    private TextView textView;

    public DataView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        textView = new TextView(context);
        textView.setTextSize(64);
        textView.setTextColor(Color.RED);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(textView);
    }

    void bindData(MainActivity.AdapterData data) {
        textView.setText(String.valueOf(data.id));
        int color = COLOR_LIST[Math.abs(data.id) % COLOR_LIST.length];
        setBackgroundColor(color);
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
                int childLeft = child.getMeasuredWidth() / 2;
                int childTop = child.getMeasuredHeight() / 2;
                child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            }
        }
    }
}
