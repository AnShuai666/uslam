package com.ubtrobot.uslam.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.ubtrobot.uslam.UslamApplication;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author leo
 * @date 2018/12/3
 * @email ao.liu@ubtrobot.com
 */
public class FileUtils {
    private static final String TAG = "FileUtils";
    public static final int CPY_BUFFER_SIZE = 4 * 1024;

    private static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 复制文件
     *
     * @param srcFile
     *            源文件路径
     * @param dstFile
     *            目标文件路径
     * @return
     */
    public static boolean copyFile(File srcFile, File dstFile) {
        boolean resu = false;

        FileInputStream fis = null;
        BufferedOutputStream fos = null;

        try {
            fis = new FileInputStream(srcFile);
            fos = new BufferedOutputStream(new FileOutputStream(dstFile));

            byte[] buffer = new byte[CPY_BUFFER_SIZE];

            int readLen = 0;

            while (-1 != (readLen = fis.read(buffer))) {
                fos.write(buffer, 0, readLen);
            }

            fos.flush();

            resu = true;
        } catch (IOException e) {
            resu = false;
        } finally {
            Closer.close(fos);
            Closer.close(fis);
        }

        return resu;
    }

    /**
     * 返回应用存储数据的根目录，sd卡优先.
     * sd卡上的: sd卡根目录/uslam
     * 内卡上的: /data/data/<包名>/files
     */
    public static String getAppFilePath() {
        boolean sdAvailable = hasSDCard();
        if (!sdAvailable) {
            return UslamApplication.getContext().getFilesDir().getAbsolutePath();
        } else {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "uslam";
            ensureDirectoryExist(path);
            return path;
        }
    }

    /**
     * 往指定路径写一个字符串
     *
     * @param path
     * @param text
     * @return
     */
    public static boolean write(String path, String text, boolean append) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (!parent.exists())
            parent.mkdirs();
        else if (!parent.isDirectory()) {
            parent.delete();
            parent.mkdirs();
        }
        FileWriter fw = null;
        try {
            file.createNewFile();
            fw = new FileWriter(file, append);
            fw.write(text);
        } catch (IOException e) {
        } finally {
            Closer.close(fw);
        }
        return false;
    }

    /**
     * 读取指定文件里的字符串
     *
     * @param path
     * @return
     */
    public static String read(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return "";
        }
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            return br.readLine();
        } catch (IOException e) {
            Log.e(TAG, "printStackTrace()--->", e);
        } finally {
            Closer.close(br);
            Closer.close(fr);
        }
        return "";
    }

    private static void ensureDirectoryExist(String dir) {
        if (TextUtils.isEmpty(dir)) {
            //do nothing
        } else {
            File file = new File(dir);
            if (!file.exists()){
                file.mkdirs();
            } else {
                if (!file.isDirectory()){
                    file.delete();
                    file.mkdirs();
                }
            }
        }
        Log.e(TAG, "路径: " + dir);
    }

    public static String getMapSavePath() {
        String path = getAppFilePath() + File.separator + "map";
        ensureDirectoryExist(path);
        return path;
    }


    /**
     * 读取json文件并且转换成字符串
     * @param filePath 文件的路径
     * @return
     * @throws IOException
     */
    public static String readJsonData(String filePath) throws IOException {
        // 读取文件数据
        //System.out.println("读取文件数据util");

        StringBuffer strbuffer = new StringBuffer();

        File myFile = new File(filePath);
        if (!myFile.exists()) {
            System.err.println("Can't Find " + filePath);
        }
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader in  = new BufferedReader(inputStreamReader);

            String str;
            while ((str = in.readLine()) != null) {
                strbuffer.append(str);
            }
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
        return strbuffer.toString();
    }

}
