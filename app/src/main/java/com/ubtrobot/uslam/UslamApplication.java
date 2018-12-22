package com.ubtrobot.uslam;

import android.app.Application;
import android.content.Context;

/**
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class UslamApplication extends Application {

    private static Context INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = getApplicationContext();
    }

    public static Context getContext() {
        return INSTANCE;
    }
}
