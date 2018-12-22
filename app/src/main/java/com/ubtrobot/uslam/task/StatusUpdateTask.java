package com.ubtrobot.uslam.task;

import com.ubtrobot.uslam.control.StatusControler;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;

public class StatusUpdateTask extends TaskExecutor {
    boolean lidar = false;
    boolean current_pose = false;
    boolean path = false;
    boolean realtime_map = false;
    boolean base64 = false;
    boolean relocalization =  false;
    boolean navigation = false;
    boolean mapping = false;
    boolean mode = false;
    boolean speed = false;
    private IRemoteRobotSdk.OnRobotStatusUpdateListener mListener;
    private int interval = 10;
    private int index = 100;

    public void setEnable(boolean lidar, boolean current_pose,
                          boolean path, boolean realtime_map,
                          boolean base64, boolean relocalization,
                          boolean navigation, boolean mapping,
                          boolean mode, boolean speed ) {
        this.lidar = lidar;
        this.current_pose = current_pose;
        this.path = path;
        this.realtime_map = realtime_map;
        this.base64 = base64;
        this.relocalization =  relocalization;
        this.navigation = navigation;
        this.mapping = mapping;
        this.mode = mode;
        this.speed = speed;
    }
    public void setRobotStatusListener(IRemoteRobotSdk.OnRobotStatusUpdateListener listener) {
        mListener = listener;
    }

    public void setRealtimeMapInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public void onExecute() {
        if(index > interval) {
            StatusControler.g().getRobotStatus(lidar, current_pose, path, realtime_map, base64, relocalization,
                    navigation, mapping, mode, speed, mListener);
            index = 0;
        } else {
            StatusControler.g().getRobotStatus(lidar, current_pose, path, false,
                    false, relocalization,
                    navigation, mapping, mode, speed, mListener);
            index += 1;
        }
    }

}