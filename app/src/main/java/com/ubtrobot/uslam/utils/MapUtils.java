package com.ubtrobot.uslam.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author leo
 * @date 2018/11/27
 * @email ao.liu@ubtrobot.com
 */
public class MapUtils {

    private static final String MAP_NAME_UMAP_SUFFIX = ".umap";
    private static final String MAP_NAME_PNG_SUFFIX = ".png";

    public static String getMapInfo(RobotMap map) {
        return "文件名称：" + map.mMapName +
                "\n" + "文件大小：" + map.mMapSize +
                "\n" + "修改时间：" + map.mCreateTime;
    }

    public static String generateMapName() {
        return "map_" + System.currentTimeMillis();
    }

    public static String removeMapNameSuffix(String fullMapName) {
        if (TextUtils.isEmpty(fullMapName)) {
            return "";
        }
        return fullMapName
                .replace(MAP_NAME_UMAP_SUFFIX, "")
                .replace(MAP_NAME_PNG_SUFFIX, "");
    }

    public static Bitmap createBitmapFromUmap(int width, int height, String umap) throws JSONException {
        JSONObject obj = new JSONObject(umap);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        JSONArray mapData = obj.getJSONArray("mapdata");
        for (int row = 0; row < height; ++row) {
            JSONArray rowData = mapData.getJSONArray(row);
            for (int rawDataIndex = 0; rawDataIndex < rowData.length(); ++rawDataIndex) {
                int data = rowData.getInt(rawDataIndex);
                for(int i = 0; i < 16; ++i) {
                    int tmp = (data & (0x03 << ( i * 2))) >>> (i * 2);
                    int color;
                    switch (tmp) {
                        case 0: {
                            color = RobotMap.COLOR_FREE;
                            break;
                        }
                        case 1: {
                            color = RobotMap.COLOR_OBSTACLE;
                            break;
                        }
                        case 2: {
                            color = RobotMap.COLOR_UNKNOWN;
                            break;
                        }
                        default:
                            color = RobotMap.COLOR_NOT_SURE;
                    }
                    int col = i + rawDataIndex * 16;
                    if(col < width) {
                        bitmap.setPixel(col, row, color);
                    }
                }
            }
        }
        return bitmap;
    }
}
