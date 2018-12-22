/*
 * Copyright (c) 2008-2016 UBT Corporation.  All rights reserved.  Redistribution,
 *  modification, and use in source and binary forms are not permitted unless otherwise authorized by UBT.
 */
package com.ubtrobot.uslam.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ubtrobot.uslam.R;


public class GsMoveControlButtons extends FrameLayout {
    private static final String TAG = "GsMoveControlButtons";
    private static final int POST_CONTROL_MOVEMENT = 0;
    private static final int CANCEL_CONTROL_MOVEMENT = 1;

    public static final int BUTTON_LEFT = 0;
    public static final int BUTTON_TOP = 1;
    public static final int BUTTON_RIGHT = 2;
    public static final int BUTTON_BOTTOM = 3;

    private ImageView left_button;
    private ImageView top_button;
    private ImageView right_button;
    private ImageView bottom_button;
    private int mRadius;
    private Handler mHandler;
    private int whichButton = -1;
    private Boolean FLAG_USEFUL = true;
    private Context mContext;
    private boolean isMoving;

    public GsMoveControlButtons(Context context) {
        this(context, null);
    }

    public GsMoveControlButtons(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GsMoveControlButtons(Context context, AttributeSet attrs,
                                int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GsMoveControlButtons(Context context, AttributeSet attrs,
                                int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private View view;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        view = LayoutInflater.from(mContext).inflate(
                R.layout.gs_move_control_buttons_layout, null, false);
        addView(view);
        view.setBackgroundResource(R.drawable.gs_rocker_bg);
        left_button = (ImageView) view.findViewById(R.id.left_button);
        top_button = (ImageView) view.findViewById(R.id.top_button);
        right_button = (ImageView) view.findViewById(R.id.right_button);
        bottom_button = (ImageView) view.findViewById(R.id.bottom_button);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case POST_CONTROL_MOVEMENT:
                        isMoving = true;
                        if (mLongPressedListener != null) {
                            mLongPressedListener.onLongPressed(whichButton);
                        }

                        if (mHandler != null) {
                            mHandler.sendEmptyMessageDelayed(POST_CONTROL_MOVEMENT, 300);
                        }
                        break;
                    case CANCEL_CONTROL_MOVEMENT:
                        //是否向机器人发送停止运动消息
                        isMoving = false;
                        if (mHandler != null) {
                            mHandler.removeMessages(POST_CONTROL_MOVEMENT);
                            if (mLongPressedListener != null) {
                                mLongPressedListener.onRelease(whichButton);
                            }
                        }

                        break;
                }
            }
        };
    }

    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels / 2, outMetrics.heightPixels / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth = 400;
        int resHeight = 400;

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY
                || heightMode != MeasureSpec.EXACTLY) {
            resWidth = getSuggestedMinimumWidth();
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;
            resHeight = getSuggestedMinimumHeight();
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else {
            resWidth = resHeight = Math.min(width, height);
        }
        setMeasuredDimension(resWidth, resHeight);
        mRadius = Math.max(getMeasuredWidth(), getMeasuredHeight());

        final int count = getChildCount();
        int childSize = mRadius;
        int childMode = MeasureSpec.EXACTLY;

        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int makeMeasureSpec = -1;
            {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,
                        childMode);
            }
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }
    }

    /**
     * 根据触摸的位置，计算角度
     *
     * @param xTouch
     * @param yTouch
     * @return
     */
    private int getAngle(float xTouch, float yTouch) {
        double x = xTouch - (mRadius / 2d);
        double y = yTouch - (mRadius / 2d);
        double angle = Math.atan2(y, x) * 180 / Math.PI;
        if (angle < 0) angle += 360;

        if (angle >= 45 && angle < 135) {
            // bottom
            whichButton = BUTTON_BOTTOM;
        } else if (angle >= 135 && angle < 225) {
            // left
            whichButton = BUTTON_LEFT;
        } else if (angle >= 225 && angle < 315) {
            whichButton = BUTTON_TOP;
        } else {
            whichButton = BUTTON_RIGHT;
        }
        Log.d(TAG, "angle=" + angle + " witch button=" + whichButton);
        return whichButton;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (FLAG_USEFUL == false) { //禁用
                    return true;
                }
                mDownTime = SystemClock.uptimeMillis();

                if (getAngle(x, y) == BUTTON_LEFT) {
                    left_button.setVisibility(View.VISIBLE);
                } else if (getAngle(x, y) == BUTTON_TOP) {
                    top_button.setVisibility(View.VISIBLE);
                } else if (getAngle(x, y) == BUTTON_RIGHT) {
                    right_button.setVisibility(View.VISIBLE);
                } else {
                    bottom_button.setVisibility(View.VISIBLE);
                }

                invalidate();
                //做50ms的延时，防触
                mHandler.sendEmptyMessageDelayed(POST_CONTROL_MOVEMENT, 50);

                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mHandler.sendEmptyMessage(CANCEL_CONTROL_MOVEMENT);

                left_button.setVisibility(View.INVISIBLE);
                top_button.setVisibility(View.INVISIBLE);
                right_button.setVisibility(View.INVISIBLE);
                bottom_button.setVisibility(View.INVISIBLE);
                invalidate();

                long now = SystemClock.uptimeMillis();
                if (performLongPressed(now - mDownTime)) {
                    return true;
                }
//                whichButton = -1;
                break;
            default:
        }
        return true;
    }

    private OnLongPressedListener mLongPressedListener;

    public void setOnLongPressedListener(OnLongPressedListener l) {
        mLongPressedListener = l;
    }

    private boolean performLongPressed(long pressedTime) {
        if (pressedTime > 1000 * 3 && mLongPressedListener != null) {
//			mLongPressedListener.onLongPressed();
            return true;
        }
        return false;
    }

    private long mDownTime;

    public interface OnLongPressedListener {

        void onLongPressed(int direction);

        void onRelease(int direction);
//        void onLeftPressed();
//
//        void onTopPressed();
//
//        void onRightPressed();
//
//        void onBottomPressed();
//
//        void onLeftUp();
//
//        void onRightUp();
//
//        void onBottomUp();
//
//        void onTopUp();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null) {
            mHandler.removeMessages(POST_CONTROL_MOVEMENT);
        }
        mHandler = null;
    }

    public void setInvalid() {
        FLAG_USEFUL = false;
        view.setBackgroundResource(R.drawable.gs_rocker_bg_disabled);
    }

    public boolean isMoving() {
        return true;
    }
}
