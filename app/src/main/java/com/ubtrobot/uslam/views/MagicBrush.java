package com.ubtrobot.uslam.views;

import android.graphics.Bitmap;

import com.ubtrobot.uslam.utils.RobotMap;

import java.util.Arrays;

public class MagicBrush {

    int mWindowSize;
    Bitmap mSource;
    Bitmap mTarget;
    int[] mColorMap = new int[]{RobotMap.COLOR_FREE, RobotMap.COLOR_OBSTACLE, RobotMap.COLOR_UNKNOWN, RobotMap.COLOR_NOT_SURE};


    public MagicBrush(Bitmap src, int windowSize) {
        mSource = src;
        mWindowSize = windowSize;
        mTarget = Bitmap.createBitmap(mSource.getWidth(), mSource.getHeight(), mSource.getConfig());
    }

    public void magic() {
        fliter();

    }

    void fliter (){
        int half = mWindowSize / 2;
        int width = mSource.getWidth();
        int height = mSource.getHeight();
        int[] pixels = new int[mWindowSize * mWindowSize];
        for(int i = half; i < width - half - mWindowSize % 2; ++i){
            for(int j = half; j < height - half - mWindowSize % 2; ++j){
                mSource.getPixels(pixels, 0, mWindowSize, i - half, j - half, mWindowSize, mWindowSize);
                int[] colorCounter = getColorCounter(pixels);
                int biggestIndex = getBiggestIndex(colorCounter);
                Arrays.fill(pixels, mColorMap[biggestIndex]);
                mTarget.setPixels(pixels, 0, mWindowSize, i - half, j - half, mWindowSize, mWindowSize);
            }
        }
    }
    private int getBiggestIndex(int[] counter) {
        int biggest = 0;
        for(int i = 1; i < counter.length; ++i) {
            if(counter[biggest] < counter[i]) {
                biggest = i;
            }
        }
        return biggest;
    }

    private int[] getColorCounter(int[] pixels) {
        int[] counter = new int[4];
        for(int i = 0; i < pixels.length; ++i ) {
            switch (pixels[i]) {
                case RobotMap.COLOR_FREE:
                    counter[0]++;
                    break;
                case RobotMap.COLOR_OBSTACLE:
                    counter[1]++;
                    break;
                case RobotMap.COLOR_UNKNOWN:
                    counter[2]++;
                    break;
                case RobotMap.COLOR_NOT_SURE:
                    counter[3]++;
                    break;
            }
        }
        return counter;
    }

    public Bitmap getTarget() {
        return mTarget;
    }
}
