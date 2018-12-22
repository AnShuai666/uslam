package com.ubtrobot.uslam.thread;

import java.util.HashMap;
import java.util.Map;

public class HandlerThreadFactory {
    public static final String TAG = "HandlerThreadFactory";

    /**
     * 后台线程:低优先级任务
     */
    public static final String BackGroundThread = "BackGround_HandlerThread";

    /**
     * 网络线程 注意会排队
     *
     */
    public static final String NetworkThread = "Network_HandlerThread";

    /**
     * 业务线程 要求耗时1s以内的
     */
    public static final String BusinessThread = "Business_HandlerThread";

    /**
     * 数据库操作
     */
    public static final String DatabaseThread = "Database_HandlerThread";

    /**
     * 专属线程:图片读写
     */
    public static final String ImageThread = "ImageThread_HandlerThread";


    /**
     * 专属线程:traceroute
     */
    public static final String TraceThread = "TraceThread_HandlerThread";

    /**
     * 线程优先级
     *
     * @param type
     * @return
     */
    private static int getPriority(String type) {
        if (BackGroundThread.equalsIgnoreCase(type)) {
            return android.os.Process.THREAD_PRIORITY_BACKGROUND;
        } else if (NetworkThread.equalsIgnoreCase(type)) {
            return android.os.Process.THREAD_PRIORITY_DEFAULT;
        } else if (BusinessThread.equalsIgnoreCase(type)) {
            return android.os.Process.THREAD_PRIORITY_DEFAULT;
        } else {
            return android.os.Process.THREAD_PRIORITY_DEFAULT;
        }
    }


    private static Map<String, BaseThread> mHandlerThreadMap = new HashMap<String, BaseThread>();

    public static BaseThread getHandlerThread(String type, boolean isDaemon) {
        BaseThread handlerThread = mHandlerThreadMap.get(type);
        if (null == handlerThread) {
            handlerThread = new BaseThread(type, getPriority(type));
            handlerThread.setDaemon(isDaemon);
            mHandlerThreadMap.put(type, handlerThread);
        } else {
            if (!handlerThread.isAlive()) {
                handlerThread.start();
            }
        }
        return handlerThread;
    }

    public static BaseThread getHandlerThread(String type) {
        BaseThread handlerThread = mHandlerThreadMap.get(type);
        if (null == handlerThread) {
            handlerThread = new BaseThread(type, getPriority(type));
            mHandlerThreadMap.put(type, handlerThread);
        } else {
            if (!handlerThread.isAlive()) {
                handlerThread.start();
            }
        }
        return handlerThread;
    }

    public static BaseThread getBusinessThread() {
        return getHandlerThread(BusinessThread);
    }

    public static BaseThread getDataBaseThread() {
        return getHandlerThread(DatabaseThread);
    }

    public static BaseThread getNetworkThread() {
        return getHandlerThread(NetworkThread);
    }
    public static BaseThread getTraceThread() {
        return getHandlerThread(TraceThread);
    }

    public static BaseThread getBackGroundThread() {
        return getHandlerThread(BackGroundThread);
    }

    public static BaseThread getImageThread() {
        return getHandlerThread(ImageThread);
    }
}
