package com.ubtrobot.uslam;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;

import com.ubtrobot.uslam.utils.ViewUtils;

/**
 * @author aoliu
 * @date 2018/12/20
 * @email ao.liu@ubtrobot.com
 */
public class AutoFitGridLayoutManager extends android.support.v7.widget.GridLayoutManager {

    private int mColumnWidth;
    private boolean mColumnWidthChanged = true;
    private Context mContext;

    public AutoFitGridLayoutManager(Context context, int columnWidth) {
        super(context, 1);
        mContext = context;
        setColumnWidth(context, columnWidth);
    }

    private void setColumnWidth(Context context, int newColumnWidth) {
        if (newColumnWidth > 0
                && newColumnWidth != mColumnWidth) {
            mColumnWidth = newColumnWidth;
            mColumnWidthChanged = true;
        }
    }

    private int checkColumnWidth(Context context, int columnWidth) {
        if (columnWidth <= 0) {
            columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 206, context.getResources().getDisplayMetrics());
        }
        return columnWidth;
    }


    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        int width = getWidth();
        int height = getHeight();

        int screenWidth = ViewUtils.getScreenWidth(mContext);
        Log.i("leo", "width: " + width
        + "\nscreenWidth: " + screenWidth
        + "\ndensity: " + ViewUtils.getDensity(mContext));
        if (mColumnWidthChanged
                && mColumnWidth > 0
                && width > 0
                && height > 0) {
            int totalWidth;
            if (getOrientation() == VERTICAL) {
                totalWidth = width - getPaddingRight() - getPaddingLeft();
            } else {
                totalWidth = height - getPaddingTop() - getPaddingBottom();
            }
            Log.i("leo", "totalWidth: " + totalWidth
            + "\ncolumnWidth: " + mColumnWidth);
            int spanCount = Math.max(1, totalWidth / mColumnWidth);
            Log.i("leo", "set max count: " + spanCount);
            setSpanCount(spanCount);
            mColumnWidthChanged = false;
        }

        super.onLayoutChildren(recycler, state);
    }
}
