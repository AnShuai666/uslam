package com.ubtrobot.uslam.utils;


import java.util.List;

public class RobotStatus {
    public List<Point> lidar;
    public Pose currentPose;
    public List<Pose> path;
    public RobotMap map;
    public SubStatus isRecalization;
    public SubStatus isNavigation;
    public SubStatus isMapping;

    public enum SubStatus {
        E_ACTION_UNKNOWN, // 0
        E_ACTION_DISCONNECT, // 1
        E_ACTION_IDLE, // 2
        E_ACTION_RUNNING, // 3
        E_ACTION_FINISHED, //4
        E_ACTION_STOP, //5
        E_ACTION_ERROR, // 6
        E_ACTION_IMPEDED, // 7
    }


}
