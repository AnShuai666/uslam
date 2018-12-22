package com.ubtrobot.uslam.utils;

public class Settings {
    private static final int HTTP_PORT = 80;
    private static final String HTTP_HEADER = "http://";

    private static final Settings ourInstance = new Settings();

    public static Settings getInstance() {
        return ourInstance;
    }

    private Settings() {
    }

    public String getBaseUrlByIp(String ip){
        return HTTP_HEADER + ip + ":" + HTTP_PORT + "/";
    }
}
