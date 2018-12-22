package com.ubtrobot.uslam.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

/**
 * @author leo
 * @date 2018/12/7
 * @email ao.liu@ubtrobot.com
 */
public class ImageUtils {

    public static Bitmap decodeBitmapFromBase64(String base64Str) {
        Bitmap thumbnail = null;
        if (!TextUtils.isEmpty(base64Str)) {
            byte[] bytes = Base64Utils.decode(base64Str);
            if (bytes != null) {
                thumbnail = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        }
        return thumbnail;
    }
}
