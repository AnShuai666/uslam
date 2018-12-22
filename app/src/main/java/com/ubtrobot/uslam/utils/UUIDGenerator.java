package com.ubtrobot.uslam.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.ubtrobot.uslam.UslamApplication;
import java.io.File;
import java.util.UUID;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

/**
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class UUIDGenerator {

    private static final String TAG = "UUIDGenerator";

    private static String mUUID;
    private static String mTempUUID;
    private static long tempUUIDGenerateTime = 0;
    /**
     * 临时tempUUID升级时间2分钟，即从生成开始，如果两分钟内仍然无法获取到权限，那升级为正式UUID
     */
    private final static int MAX_RETRY_TIME = 2 * 60 * 1000;
    private final static String INSTALLATION = "INSTALLATION";

    //对外提供的
    public static String getUUID() {
        if (!TextUtils.isEmpty(mUUID)) {
            Log.i(TAG, "1.命中内存缓存uuid");
            return mUUID;
        }
        // 判断缓存是否有正式的uuid
        if (isCachedUUID(INSTALLATION)) {
            String uuid = readInstallationFile(INSTALLATION, false);
            if (!TextUtils.isEmpty(uuid)) {
                // 获取App应用缓存目录下的数据成功，那直接使用
                Log.i(TAG, "2.命中应用缓存uuid");
                mUUID = uuid;
                return mUUID;
            } else {
                // 获取App应用缓存目录下的数据失败，那再获取SD卡上的数据，成功则返回，失败则生成临时的
                uuid = readInstallationFile(INSTALLATION, true);
                if (!TextUtils.isEmpty(uuid)) {
                    Log.i(TAG, "3.命中sd卡缓存uuid");
                    mUUID = uuid;
                    syncIdFile();
                    return mUUID;
                } else {
                    //获取失败需要生成临时的uuid
                    if (TextUtils.isEmpty(mTempUUID)) {
                        // 临时的uuid在一个生命周期内也只生成一次，还是之前说的要尽可能延长期生命周期
                        mTempUUID = getTempUUID();
                        tempUUIDGenerateTime = System.currentTimeMillis();
                        Log.i(TAG, "4.生成临时tempUUID");
                    }
                    if ((System.currentTimeMillis() - tempUUIDGenerateTime) >= MAX_RETRY_TIME) {
                        // 超过重试次数之后，mTempUUID升级成mUUID，并且写在持久缓存中
                        mUUID = mTempUUID;
                        writeInstallationFile(mUUID, INSTALLATION);
                        Log.i(TAG, "6.临时tempUUID升级成UUID");
                    }
                    Log.i(TAG, "5.命中临时tempUUID");
                    return mTempUUID;
                }
            }

        } else {
            // 如果本地没有缓存，那直接生成然后缓存
            mUUID = getTempUUID();
            Log.i(TAG, "7.本地没有缓存的UUID，生成并缓存");
            writeInstallationFile(mUUID, INSTALLATION);
            return mUUID;
        }
    }

    private synchronized static void syncIdFile() {
        File installation = new File(UslamApplication.getContext().getFilesDir(), INSTALLATION);
        File installationSD = new File(FileUtils.getAppFilePath(), INSTALLATION);
        //只要app应用存储下有数据，不管sd卡中有没有都往里面同步一次，避免sd卡数据补修改的问题
        if (installation.exists()) {
            FileUtils.copyFile(installation, installationSD);
        } else if (!installation.exists() && installationSD.exists()) {
            FileUtils.copyFile(installationSD, installation);
        }
    }

    private static String readInstallationFile(String installationPath, boolean isFromSD) {
        File installation;
        if (isFromSD) {
            installation = new File(FileUtils.getAppFilePath(), installationPath);
        } else {
            installation = new File(UslamApplication.getContext().getFilesDir(), installationPath);
        }

        return FileUtils.read(installation.getAbsolutePath());
    }


    private static void writeInstallationFile(String uuid, String installationPath) {
        File installation = new File(UslamApplication.getContext().getFilesDir(), installationPath);
        File installationSD = new File(FileUtils.getAppFilePath(), installationPath);
        FileUtils.write(installation.getAbsolutePath(), uuid, false);
        FileUtils.write(installationSD.getAbsolutePath(), uuid, false);
    }

    private static boolean hasSDPermission() {
        boolean hasSDPermission;
        if (SDK_INT < M) {
            hasSDPermission = true;
        } else {
            hasSDPermission = UslamApplication.getContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
        }
        return hasSDPermission;
    }

    private static boolean isCachedUUID(String installationPath) {
        File installation = new File(UslamApplication.getContext().getFilesDir(), installationPath);
        File installationSD = new File(FileUtils.getAppFilePath(), installationPath);
        return installation.exists() || (installationSD.exists());
    }

    private static String getTempUUID() {
        String result = "";
        //这里的获取方式应该是先以设备信息为维度来生成，失败的情况下再随机生成
        final TelephonyManager tm = (TelephonyManager) UslamApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String tmDevice, tmSerial, androidId;
        //为了避免影响原先版本已生成DeviceKey的逻辑，还是需要先走一次这个逻辑
        try {
            if (ActivityCompat.checkSelfPermission(UslamApplication.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: 2018/12/4 no permission
            }
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = ""
                    + android.provider.Settings.Secure.getString(
                    UslamApplication.getContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e1) {
            Log.e(TAG, "printStackTrace()--->", e1);
            // 在原有生成规则异常的情况下，使用单独某一个key进行处理
            try {
                tmDevice = "" + tm.getDeviceId();
            } catch (Exception e) {
                Log.e(TAG, "printStackTrace()--->", e);
                tmDevice = "";
            }
            try {
                tmSerial = "" + tm.getSimSerialNumber();
            } catch (Exception e) {
                Log.e(TAG, "printStackTrace()--->", e);
                tmSerial = "";
            }
            try {
                androidId = ""
                        + android.provider.Settings.Secure.getString(
                        UslamApplication.getContext().getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                Log.e(TAG, "printStackTrace()--->", e);
                androidId = "";
            }

        }
        if (TextUtils.isEmpty(tmDevice) && TextUtils.isEmpty(tmSerial) && TextUtils.isEmpty(androidId)) {
            result = UUID.randomUUID().toString();
        } else {
            UUID deviceUuid = new UUID(androidId.hashCode(),
                    ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            result = deviceUuid.toString();
        }
        return result;
    }
}
