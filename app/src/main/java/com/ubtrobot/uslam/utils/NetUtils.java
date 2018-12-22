package com.ubtrobot.uslam.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import com.ubtrobot.uslam.UslamApplication;

/**
 * @author leo
 * @date 2018/12/12
 * @email ao.liu@ubtrobot.com
 */
public class NetUtils {

    public static String getConnectWifiSsid() {
        String ssid = "";
        Context context = UslamApplication.getContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WifiManager manager =
                    (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            if (info != null) {
                ssid = info.getSSID();
            }
        } else {
            ConnectivityManager manager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) {
                ssid = info.getExtraInfo();
            }
        }
//        Log.i("leo", "wifi name: " + ssid);
        return ssid.replace("\"", "");
    }
}
