package com.ubtrobot.uslam.net;

/**
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class HttpRet {

    public static final int NET_ERROR = -1;

    public static final int SUCCESS = 200;

    public static final int MISSION_FINISHED = 201;

    public static final int MISSION_IS_DOING = 202;

    public static final int ROBOT_BUSY = 401;

    public static final int ROBOT_TRAPPED = 402;

    public static final int ROBOT_EMERGENCY_STOP = 403;

    public static final int NOT_FOUND = 404;

    public static final int COMMAND_CAN_NOT_EXECUTE = 405;
}
