package com.ubtrobot.uslam.sdk;

import android.content.Context;

import com.ubtrobot.uslam.control.MotionControler;
import com.ubtrobot.uslam.utils.Line;
import com.ubtrobot.uslam.utils.Point3D;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.Result;
import com.ubtrobot.uslam.utils.Robot;
import com.ubtrobot.uslam.utils.RobotMap;
import com.ubtrobot.uslam.utils.RobotStatus;

import java.util.List;
import java.util.UUID;

public interface IRemoteRobotSdk {

    void setContext(Context context);

    interface OnRobotUpdateListener {
      public void onRobotUpdate(Robot robot);
    }

    interface OnRobotListUpdateListener {
        public void onRobotListUpdate(List<Robot> robot);
    }

    interface OnRobotStatusUpdateListener {
        public void onRobotStatusUpdate(RobotStatus status);
    }

    interface OnMapListUpdateListener {
        public void onMapListUpdate(List<RobotMap> maps);
    }

    interface OnMapUpdateListener {
        public void onMapUpdate(RobotMap map);
    }

    interface OnResultListener {
        public void onResult(Result result);
    }

    interface OnExportMapListener {
        public void onSuccess(String map);

        public void onFailure();
    }

    public void setUUID(UUID accessToken);

    /**
     * get the robot list from the UDP port.
     * [code. 1.1]
     *
     * @param listener callback for the
     */
    public void requestRobotList(OnRobotListUpdateListener listener);

    /**
     * connect a robot.
     * [code 2.1]
     * @param baseUrl the http base url for the sdk.
     * @param listener callback
     */
    public void connectRobot(String baseUrl, OnRobotUpdateListener listener);

    /**
     * connect a robot.
     * [code 2.2]
     */
    public void disconnectRobot();

    /**
     * request robot status including the lidar, realtime mBitmap, current pose , path,
     * and is re-localization..
     * [code 2.3]
     * @param listener callback
     */
    public void requestRobotStatusUpdate(OnRobotStatusUpdateListener listener );

    /**
     * send a request to move the robot.
     *
     * it cannot be using when robot are doing a mission.
     * [code 2.4]
     *
     * @param linear linear speed. the x is the forward of the robot.
     * @param angular angular speed. the z is the turn left.
     * @param resultListener callback
     */
    public void requestMoveRobot(Point3D linear, Point3D angular, OnResultListener resultListener);

    /**
     * request robot to do a re-localization mission.
     * the system will set the current pose of robot to initPose, and the robot
     * will turn around for making the particles convergence.
     *
     * [code 3.1]
     *
     * @param initPose the actual pose of the robot.
     * @param resultListener the callback, the implementation should call this callback after using
     *                      the [code 2.3] to make sure the relocalization has finished.
     */
    public void requestRelocalization(Pose initPose, OnResultListener resultListener);

    /**
     * set the navigation target. the target is a pose list, the robot will goto the pose in the target
     * one by one.
     *[code 3.2]
     *
     * @param target the list of the pose.
     * @param resultListener callback. this callback will be call after all the mission has been finished.
     *                       or cancel.
     */
    public void setTarget(List<Pose> target, OnResultListener resultListener);

    /**
     * cancel the navigation by user.
     * [code 3.3]
     *
     * @param resultListener callback.
     */
    public void cancelNavigation(OnResultListener resultListener);

    /**
     * request to get all maps in this robot.
     *[code 4.1]
     * @param listener callback.
     */
    public void requestMapList(OnMapListUpdateListener listener);

    /**
     * get the entire mBitmap. the robot will load this mBitmap at same time
     * [code 4.2]
     *
     * @param mapName the mBitmap name is always unicode in a robot.
     * @param isPng if true, return png map
     * @param isUmap if true, return umap
     * @param isLoad if true, the mBitmap will load at robot
     * @param isArchive if true, return the packaged map information in the file stream.
     * @param listener callback
     */
    public void requestMap(String mapName,
                           boolean isPng,
                           boolean isUmap,
                           boolean isLoad,
                           boolean isArchive,
                           OnMapUpdateListener listener);

    /**
     * get the current mBitmap.
     * [code 4.2]
     *
     * @param canEdit if true, will get the occupancy_grid at callback.
     * @param listener callback
     */
    public void requestCurrentMap(boolean canEdit,
                                  OnMapUpdateListener listener);
    /**
     * export mBitmap as file
     *
     * [code 4.3]
     * @param mapName the mBitmap name is always unicode in a robot.
     * @param listener callback
     */
    public void exportMap(String mapName, OnExportMapListener listener);

    /**
     * delete mBitmap at robot.
     *[code 4.5]
     * @param mapName the mBitmap will be deleted.
     * @param listener callback.
     */
    public void deleteMap(String mapName, OnResultListener listener);

    /**
     * import mBitmap to robot.
     *[code 4.6]
     * @param map the mBitmap must can be edited.
     * @param listener callback.
     */
    public void importMap(RobotMap map, OnResultListener listener);

    /**
     * update the mapOldName mBitmap.
     *[code 4.4]
     *
     * @param map the mBitmap instance.
     * @param listener callback.
     */
    public void updateMap(RobotMap map, OnResultListener listener);

    /**
     * start mapping.
     *
     * [code 4.7]
     *
     * @param listener callback
     */
    public void requestStartMapping(OnResultListener listener);


    /**
     * update mapping.
     *
     * [code 4.10]
     *
     * @param listener callback
     */
    public void requestUpdateMapping(OnResultListener listener);

    /**
     * stop mapping.
     * [code 4.8]
     *
     * @param mapName the mBitmap name must be unicode at robot.
     * @param wall the virtual wall
     * @param tracker the tracker
     * @param listener callback
     */
    public void requestStopMapping(String mapName,
                                   List<Line> wall,
                                   List<Line> tracker,
                                   OnResultListener listener);

    /**
     *  cancel relocalization
     *  [code 3.2]
     *
     * @param listener callback
     */
    public void cancelRelocalization(OnResultListener listener);

    /**
     * query relocalization
     * [code 3.3]
     *
     * @param listener callback
     */
    public void queryRelocalization(OnResultListener listener);

    /**
     *  query navigation status
     *  [code 3.6]
     *
     * @param listener
     */
    public void queryNavigationStatus(OnResultListener listener);

    /**
     *  get current connecting robot.
     *
     * @return
     */
    public Robot getCurrentRobot();

    /**
     *
     * @param speed
     * @param direction
     * @param listener
     */
    public void simpleMove(MotionControler.Speed speed, MotionControler.Direction direction, IRemoteRobotSdk.OnResultListener listener);

    /**
     *
     * @param listener
     */
    public void stopMove(OnResultListener listener);

}
