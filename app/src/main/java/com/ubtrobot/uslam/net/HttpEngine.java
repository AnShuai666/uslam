package com.ubtrobot.uslam.net;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class HttpEngine {

    private static final String TAG = "HttpEngine";

//    private static String BASE_URL = "http://192.168.5.3:8083";
    private static String BASE_URL = null;

    private static <T> T createService(String baseUrl, Class<T> cls) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClientHelper.getOkHttpClient())
                .build();
        return retrofit.create(cls);
    }

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
        Log.i("okhttp", "baseUrl: " + baseUrl);
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static void removeBaseUrl() {
        BASE_URL = null;
        Log.i("okhttp", "remove baseUrl");
    }

    public static UslamService createUslamService() {
        return createService(BASE_URL, UslamService.class);
    }

}
