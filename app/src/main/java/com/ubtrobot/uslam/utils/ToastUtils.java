package com.ubtrobot.uslam.utils;

import android.os.Looper;
import android.widget.Toast;
import com.ubtrobot.uslam.UslamApplication;

/**
 * @author leo
 * @date 2018/12/5
 * @email ao.liu@ubtrobot.com
 */
public class ToastUtils {

    public static void show(String message) {
        Toast.makeText(UslamApplication.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showToastLong(String message) {
        Toast.makeText(UslamApplication.getContext(), message, Toast.LENGTH_LONG).show();
    }

    public static void show(int message) {
        Toast.makeText(UslamApplication.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showToastOnSubThread(String message) {
        try {
            show(message);
        } catch (Exception e) {
            Looper.prepare();
            show(message);
            Looper.loop();
        }
    }
}
