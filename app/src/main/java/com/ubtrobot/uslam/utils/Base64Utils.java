package com.ubtrobot.uslam.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

/**
 * @author leo
 * @date 2018/12/6
 * @email ao.liu@ubtrobot.com
 */
public class Base64Utils {

    public static String encode(String s) {
        return null;
    }


    @TargetApi(Build.VERSION_CODES.O)
    public static byte[] decode(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            byte[] bytes = Base64.decode(str, Base64.DEFAULT);
            for (int i = 0, j = bytes.length; i < j; ++i) {
                // 调整异常数据
                if (bytes[i] < 0) {
                    bytes[i] += 256;
                }
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
