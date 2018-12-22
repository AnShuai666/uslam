package com.ubtrobot.uslam.sdk;

import android.content.Context;

public class SdkManager {

    private static IRemoteRobotSdk sSdk = null;

    private SdkManager() {
    }

    public void setConetxt(Context context) {
        sSdk.setContext(context);
    }

    public static IRemoteRobotSdk getSdk() {
        if(sSdk == null) {
//            sSdk = new FakeLocalSdk();
            sSdk = new RobotSdkImp();
        }
        return sSdk;
    }

}
