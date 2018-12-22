package com.ubtrobot.uslam.utils;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import com.ubtrobot.uslam.UslamApplication;

/**
 * @author leo
 * @date 2018/12/18
 * @email ao.liu@ubtrobot.com
 */
public class AppUtils {

    public static String getString(@StringRes int res) {
        return UslamApplication.getContext().getString(res);
    }

    public static int getColor(@ColorRes int res) {
        return UslamApplication.getContext().getResources().getColor(res);
    }
}
