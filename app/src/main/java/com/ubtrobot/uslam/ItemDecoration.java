package com.ubtrobot.uslam;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author aoliu
 * @date 2018/12/20
 * @email ao.liu@ubtrobot.com
 */
public class ItemDecoration extends RecyclerView.ItemDecoration {

    private int mTop;
    private int mLeft;
    private int mBottom;
    private int mRight;

    public ItemDecoration(int top, int left, int bottom, int right) {
        mTop = top;
        mLeft = left;
        mBottom = bottom;
        mRight = right;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildLayoutPosition(view);
        if (position != 0) {
            outRect.set(mLeft, mTop, mRight, mBottom);
        }
    }
}
