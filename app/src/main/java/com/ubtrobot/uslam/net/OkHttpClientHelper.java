package com.ubtrobot.uslam.net;

import android.util.Log;
import com.ubtrobot.uslam.utils.DeviceUtils;
import com.ubtrobot.uslam.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class OkHttpClientHelper {

    private static OkHttpClient mOkHttpClient = null;

    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            mOkHttpClient = createInstance();
        }
        return mOkHttpClient;
    }

    private static OkHttpClient createInstance() {
        // 日志拦截器
        LoggerInterceptor httpLoggingInterceptor = new LoggerInterceptor();
        // 设定日志级别
        httpLoggingInterceptor.setLevel(LoggerInterceptor.Level.BODY);
        ArrayList<String> filters = new ArrayList<>();
        filters.add("robot/status");
        httpLoggingInterceptor.addFilter(filters);
        // 请求拦截器
        RequestInterceptor requestInterceptor = new RequestInterceptor();
        // 响应拦截器
        ResponseInterceptor responseInterceptor = new ResponseInterceptor();

        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .readTimeout(600, TimeUnit.SECONDS)
                .connectTimeout(600, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(requestInterceptor)
                .addInterceptor(responseInterceptor)
                .build();
        return client;
    }

    static class RequestInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            // add headers for all request.
            request = request.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("access-token", DeviceUtils.getUUID())
                    .build();
            return chain.proceed(request);
        }
    }

    static class ResponseInterceptor implements Interceptor {

        private static final String TAG = "ResponseInterceptor";

        @Override
        public Response intercept(Chain chain) throws IOException {
            // process some special response code
//            Log.e(TAG, chain.request().toString());
            Response response = chain.proceed(chain.request());
            int code = response.code();
            switch (code) {
                case HttpRet.ROBOT_BUSY:
                    ToastUtils.showToastOnSubThread("robot busy!");
                    break;
                case HttpRet.ROBOT_TRAPPED:
                    ToastUtils.showToastOnSubThread("robot trapped!");
                    break;
                case HttpRet.ROBOT_EMERGENCY_STOP:
                    ToastUtils.showToastOnSubThread("robot emergency stop!");
                    break;
                case HttpRet.NOT_FOUND:
                    ToastUtils.showToastOnSubThread("robot not found!");
                    break;
                case HttpRet.COMMAND_CAN_NOT_EXECUTE:
                    ToastUtils.showToastOnSubThread("command can not execute!");
                    break;
                    default:
            }
            return response;
        }
    }
}
