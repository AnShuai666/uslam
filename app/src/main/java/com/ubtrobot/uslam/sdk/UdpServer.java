package com.ubtrobot.uslam.sdk;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.ubtrobot.uslam.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @author leo
 * @date 2018/12/12
 * @email ao.liu@ubtrobot.com
 */
public class UdpServer {

    private static final String TAG = "UdpServer";
    private static final int UDP_SERVER_PORT = 8085;
    private static final int MAX_UDP_DATAGRAM_LEN = 1500;
    private static final int NEW_ROBOT = 1020;
    private static final int WATCH_INTERVAL = 5000;
    private boolean mRunUdpOnce = false;

    private OnReceiveDataPacketListener mListener;

    private boolean isServerRunning;

    private Runnable watchRunnable = new Runnable() {
        @Override
        public void run() {
            if(!isServerRunning) {
                Log.w(TAG, "udp server is disconnect,please check it!");
            }
            mHandler.postDelayed(watchRunnable, WATCH_INTERVAL);
            isServerRunning = false;
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NEW_ROBOT:
                    try {
                        JSONObject object = new JSONObject((String) (msg.obj));
                        if (mListener != null) {
                            mListener.onReceive(object);
                        }
                        Log.i(TAG, "onReceive " + object);
                    } catch (JSONException e) {

                    }
                    break;
                default:
                    break;
            }
        }

    };

    private static class UdpServerHolder {
        private static UdpServer INSTANCE = new UdpServer();
    }

    public static UdpServer getInstance() {
        return UdpServerHolder.INSTANCE;
    }

    private UdpServer() {

    }

    private void runUdpServer() {
        if(mRunUdpOnce) {
            return;
        }
        new Thread(() -> {
            byte[] buffer = new byte[MAX_UDP_DATAGRAM_LEN];
            DatagramSocket dataSocket = null;
            try {
                dataSocket = new DatagramSocket(UDP_SERVER_PORT);
                DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
                while(true) {
                    dataSocket.receive(dataPacket);
                    Message msg = new Message();
                    msg.what = NEW_ROBOT;
                    msg.obj= new String(dataPacket.getData(),0 , dataPacket.getLength() - 1);
                    mHandler.sendMessage(msg);
                    dataPacket.setLength(buffer.length);
                    isServerRunning = true;
                }
            } catch (SocketException e) {
                Log.e(TAG, "msg: error" + e );
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "msg: error" + e );
                e.printStackTrace();
            } finally {
                Log.e(TAG, "msg: finnaly" );
                if (dataSocket != null) {
                    dataSocket.close();
                }
            }
        }).start();
        mRunUdpOnce = true;
    }

    public void start(OnReceiveDataPacketListener listener) {
        mListener = listener;
        runUdpServer();
        mHandler.postDelayed(watchRunnable, WATCH_INTERVAL);
    }

    public interface OnReceiveDataPacketListener {
        void onReceive(JSONObject json);
    }
}
