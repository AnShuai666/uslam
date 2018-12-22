package com.ubtrobot.uslam.thread;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;


/**
 * @author alexlong
 * 
 * 通过dispatchMessage耗时记录线程耗时
 * 
 */
public class BaseHandler extends Handler {

    private static final String tag = "BaseHandler";
    private static final long Time_Lv1 = 20;
    private static final long Time_Lv2 = 100;
    private static final long Time_Lv3 = 500;
    private Callback mCallbackEx;

    public BaseHandler() {
        super();
    }

    public BaseHandler(Looper looper) {
        super(looper);
    }

//     public BaseHandler(Looper looper, Handler.Callback callback) {
//         super(looper, callback);
//         mCallbackEx = callback;
//     }

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
//        long startTime = System.currentTimeMillis();
        boolean rst = super.sendMessageAtTime(msg, uptimeMillis);
        //long endTime = System.currentTimeMillis();
        //long span = endTime - startTime;
        //printSendInfo(span, msg);
        return rst;
    }

//    private void printSendInfo(long span, Message msg) {
//        String flag = " Message What:";
//        try {
//            flag += msg.what;
//            if (null != msg.getCallback()) {
//                flag += " Runnable-" + msg.getCallback().getClass().toString();
//            } else if (null != mCallbackEx) {
//                flag += " Callback-" + mCallbackEx.getClass().toString();
//            } else {
//                flag += " handleMessage-" + this.getClass().toString();
//            }
//        } catch (NullPointerException ne) {
//            // 打印日志的地方,防止getClass出现null,统一catch
//        } catch (Exception e) {
//
//        }
//
//        String tagDispatch = " SendMessage-" + Thread.currentThread().getName() + ":";
//        if (span > Time_Lv3) {
//            LogUtil.e(tag, tagDispatch + span + "ms " + flag);
//        } else if (span > Time_Lv2) {
//        	LogUtil.w(tag, tagDispatch + span + "ms " + flag);
//        } else if (span > Time_Lv1) {
//        	LogUtil.i(tag, tagDispatch + span + "ms " + flag);
//        } else {
//        	LogUtil.d(tag, tagDispatch + span + "ms " + flag);
//        }
//    }

    @Override
    public void dispatchMessage(Message msg) {
        long realTimeStart = SystemClock.elapsedRealtime();
        long threadTimeStart = SystemClock.currentThreadTimeMillis();
        super.dispatchMessage(msg);
        long realTimeSpanEnd = SystemClock.elapsedRealtime();
        long threadTimeSpanEnd = SystemClock.currentThreadTimeMillis();
        //printDispatchInfo(span, msg);
    }

    /**
     * 打印日志 
     * 日志分析说明:
     * 
     * Runnable模式,是post runnable到handler里面的,这里大部分是匿名类
     * 打印出XXX$1这样的类似日志，这个XXX可以定位到类，$1标识XXX类里面第一个匿名类(记录从0开始)，通过这样的方式可以定位到代码
     * 
     * Callback模式,就是相关类实现了Callback接口,然后将接口传递到Handler里面
     * 这种情况可以直接定位到代码里面的handleMessage函数.
     * 
     * handleMessage模式,就是直接继承Handler类,然后重新handleMessage方法.
     * 这种情况也可以直接定位到代码.
     * 
     * @param span
     * @param msg
     */
    private void printDispatchInfo(long span, Message msg) {
        String flag = " Message What:";
        try {
            flag += msg.what;
            if (null != msg.getCallback()) {
                flag += " Runnable-" + msg.getCallback().getClass().toString();
            } else if (null != mCallbackEx) {
                flag += " Callback-" + mCallbackEx.getClass().toString();
            } else {
                flag += " handleMessage-" + this.getClass().toString();
            }
        } catch (NullPointerException ne) {
            // 打印日志的地方,防止getClass出现null,统一catch
        } catch (Exception e) {

        }

        String tagDispatch = "DispatchMessage-" + Thread.currentThread().getName() + ":";
        if (span > Time_Lv3) {
        	Log.e(tag, tagDispatch + span + "ms " + flag);
        } else if (span > Time_Lv2) {
            Log.w(tag, tagDispatch + span + "ms " + flag);
        } else if (span > Time_Lv1) {
            Log.i(tag, tagDispatch + span + "ms " + flag);
        } else {
            Log.d(tag, tagDispatch + span + "ms " + flag);
        }
    }

    public Callback getCallbackEx() {
        return mCallbackEx;
    }

    
    
}
