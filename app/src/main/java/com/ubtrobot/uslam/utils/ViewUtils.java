package com.ubtrobot.uslam.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * @author leo
 * @date 2018/12/10
 * @email ao.liu@ubtrobot.com
 */
public class ViewUtils {

    public static float getDensity(Context context) {
       return context.getResources().getDisplayMetrics().density;
    }

    public static int dp2px(Context context, int dp) {
        return (int) (getDensity(context) * dp + 0.5f);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

}
