package com.ubtrobot.uslam.utils;

import android.util.Log;
import java.io.Closeable;
import java.io.IOException;

/**
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class Closer {
    private static final String TAG = "Closer";
    public static void close(Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, "printStackTrace()--->", e);
        }
    }
}
