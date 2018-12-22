package com.ubtrobot.uslam.control;

import com.ubtrobot.uslam.views.GsMoveControlButtons;

/**
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class MotionParser {

    public static String paserSpeed(MotionControler.Speed speed) {
        switch (speed) {
            case LOW:
                return "low";
            case NORMAL:
                return "normal";
            case HIGH:
                return "high";
            default:
                return "normal";
        }
    }

    public static String parseDirection(MotionControler.Direction direction) {
        switch (direction) {
            case LEFT:
                return "left";
            case RIGHT:
                return "right";
            case FORWARD:
                return "forward";
            case BACKWARD:
                return "backward";
            case STOP:
            default:
                return "stop";
        }
    }

    public static MotionControler.Direction parseKeyDirection(int keyDirection) {
        MotionControler.Direction direction;
        switch (keyDirection) {
            case GsMoveControlButtons
                    .BUTTON_LEFT:
                direction = MotionControler.Direction.LEFT;
            break;
            case GsMoveControlButtons
                    .BUTTON_TOP:
                direction = MotionControler.Direction.FORWARD;
                break;
            case GsMoveControlButtons
                    .BUTTON_RIGHT:
                direction = MotionControler.Direction.RIGHT;
                break;
            case GsMoveControlButtons
                    .BUTTON_BOTTOM:
                direction = MotionControler.Direction.BACKWARD;
                break;
                default:
                    direction = MotionControler.Direction.STOP;
        }
        return direction;
    }
}
