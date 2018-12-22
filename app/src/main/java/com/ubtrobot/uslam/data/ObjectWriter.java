package com.ubtrobot.uslam.data;

import android.util.Log;

import com.google.gson.Gson;
import com.ubtrobot.uslam.utils.Closer;
import com.ubtrobot.uslam.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author leo
 * @date 2018/12/14
 * @email ao.liu@ubtrobot.com
 */
public class ObjectWriter {

    public static <T> void write(String saveDir, T obj, String fileName) {

        Gson gson = new Gson();
        String s = gson.toJson(obj);

        FileOutputStream fos = null;
        try {

            byte[] bytes = s.getBytes();

            File file = new File(saveDir, fileName);
            fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closer.close(fos);
        }
    }

    public static <T> T read(String saveDir, String fileName, Class<T> cls) {
//        File file = new File(saveDir, fileName);
        String read = null;
        try {
            read = FileUtils.readJsonData(saveDir + File.separator + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        T t = gson.fromJson(read, cls);
        return t;
    }
}
