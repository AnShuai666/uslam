package com.ubtrobot.uslam.task;

import android.os.Handler;

/**
 * @author leo
 * @date 2018/12/10
 * @email ao.liu@ubtrobot.com
 */
public abstract class TaskExecutor {

    private Handler mHandler;
    private Runnable mRunable;
    private boolean isExecuting;
    private int interval;
    private boolean isLoop;

    public TaskExecutor() {
        mHandler = new Handler();
    }

    public void start(boolean isLoop, int interval) {
        this.interval = interval;
        this.isLoop = isLoop;

        if (this.isLoop) {
            mRunable = () -> {
                onExecute();
                mHandler.postDelayed(mRunable, this.interval);
            };
        } else {
            mRunable = ()-> onExecute();
        }
        mHandler.post(mRunable);
        isExecuting = true;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setIsLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }

    public void cancel() {
        mHandler.removeCallbacks(mRunable);
        isExecuting = false;
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    /**
     * The task where you can execute.
     */
    public abstract void onExecute();
}
