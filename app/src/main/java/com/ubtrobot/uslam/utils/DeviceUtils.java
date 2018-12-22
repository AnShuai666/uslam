package com.ubtrobot.uslam.utils;

import android.util.DisplayMetrics;

import com.ubtrobot.uslam.UslamApplication;

/**
 * @author leo
 * @date 2018/12/3
 * @email ao.liu@ubtrobot.com
 */
public class DeviceUtils {


    public static String getUUID() {
        return UUIDGenerator.getUUID();
    }

    public static int getScreenWidth() {
        DisplayMetrics metrics =
                UslamApplication.getContext().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics metrics =
                UslamApplication.getContext().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}
