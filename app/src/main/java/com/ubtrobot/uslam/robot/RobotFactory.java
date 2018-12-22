package com.ubtrobot.uslam.robot;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.utils.AppUtils;
import com.ubtrobot.uslam.utils.OnLineStatus;
import com.ubtrobot.uslam.utils.Robot;

/**
 * @author leo
 * @date 2018/12/18
 * @email ao.liu@ubtrobot.com
 */
public class RobotFactory {

    private static final String BASE_URL_OFFLINE = "http://robot/offline";
    private static final String BASE_URL_ADD = "http://robot/add";

    public static Robot createOfflineRobot() {
        Robot robot = new Robot();
        robot.onLineStatus = OnLineStatus.Offline;
        robot.name = AppUtils.getString(R.string.offline_edit);
        robot.baseURL = BASE_URL_OFFLINE;
        return robot;
    }

    public static Robot createAddRobot() {
        Robot robot = new Robot();
        robot.baseURL = BASE_URL_ADD;
        return robot;
    }
}
