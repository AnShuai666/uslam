package com.ubtrobot.uslam.control;

import com.ubtrobot.uslam.utils.OnLineStatus;
import com.ubtrobot.uslam.utils.RobotStatus;

/**
 * @author leo
 * @date 2018/12/17
 * @email ao.liu@ubtrobot.com
 */
public class RobotStatusPaser {

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_DISCONNECT = 1;
    public static final int STATUS_IDLE = 2;
    public static final int STATUS_RUNNIG = 3;
    public static final int STATUS_FINISHED = 4;
    public static final int STATUS_STOP = 5;
    public static final int STATUS_ERROR = 6;
    public static final int STATUS_IMPEDED = 7;

    public static RobotStatus.SubStatus parseSubStatus(int code) {
        RobotStatus.SubStatus status;
        switch (code) {
            case STATUS_UNKNOWN:
                status = RobotStatus.SubStatus.E_ACTION_UNKNOWN;
                break;
            case STATUS_DISCONNECT:
                status = RobotStatus.SubStatus.E_ACTION_DISCONNECT;
                break;
            case STATUS_IDLE:
                status = RobotStatus.SubStatus.E_ACTION_IDLE;
                break;
            case STATUS_RUNNIG:
                status = RobotStatus.SubStatus.E_ACTION_RUNNING;
                break;
            case STATUS_FINISHED:
                status = RobotStatus.SubStatus.E_ACTION_FINISHED;
                break;
            case STATUS_STOP:
                status = RobotStatus.SubStatus.E_ACTION_STOP;
                break;
            case STATUS_ERROR:
                status = RobotStatus.SubStatus.E_ACTION_ERROR;
                break;
            case STATUS_IMPEDED:
                status = RobotStatus.SubStatus.E_ACTION_IMPEDED;
                break;
            default:
                status = RobotStatus.SubStatus.E_ACTION_UNKNOWN;
        }
        return status;
    }

    public static OnLineStatus parseOnLineStatus(String status) {
        OnLineStatus onLineStatus;
        switch (status) {
            case "online":
                onLineStatus = OnLineStatus.Online;
                break;
            case "offline":
                onLineStatus = OnLineStatus.Offline;
                break;
            case "busy":
                onLineStatus = OnLineStatus.Busy;
                break;
            default:
                onLineStatus = OnLineStatus.Offline;

        }
        return onLineStatus;
    }
}
