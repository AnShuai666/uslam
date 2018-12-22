package com.ubtrobot.uslam.sdk;

import android.content.Context;
import android.util.Log;

import com.ubtrobot.uslam.control.MapControler;
import com.ubtrobot.uslam.control.MotionControler;
import com.ubtrobot.uslam.control.NavigationControler;
import com.ubtrobot.uslam.control.StatusControler;
import com.ubtrobot.uslam.fragments.RobotFragment;
import com.ubtrobot.uslam.net.HttpEngine;
import com.ubtrobot.uslam.robot.RobotFactory;
import com.ubtrobot.uslam.utils.Line;
import com.ubtrobot.uslam.utils.OnLineStatus;
import com.ubtrobot.uslam.utils.Point3D;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.Result;
import com.ubtrobot.uslam.utils.Robot;
import com.ubtrobot.uslam.utils.RobotMap;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class RobotSdkImp implements IRemoteRobotSdk {

    private static final String TAG = "RobotSdkImp";

    private LinkedList<Robot> mRobots;
    private Context mContext;

    private OnRobotListUpdateListener mRequestRobotListListener;

    public RobotSdkImp(){
        UdpServer.getInstance().start(this::updateRobotList);
        mRobots = new LinkedList<>();
        mRobots.addFirst(RobotFactory.createAddRobot());
        mRobots.addFirst(RobotFactory.createOfflineRobot());
    }

    public LinkedList<Robot> getRobots() {
        return mRobots;
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
    }



    @Override
    public void setUUID(UUID accessToken) {

    }

    private void updateRobotList(JSONObject object) {
        for(Robot robot : mRobots) {
            if (robot.equals(object)) {
                robot.set(object);
                return;
            }
        }
        Robot robot = new Robot(object);
        mRobots.addFirst(robot);
        Log.e(TAG, object.toString());
        if(mRequestRobotListListener != null) {
            mRequestRobotListListener.onRobotListUpdate(mRobots);
        }
    }

    @Override
    public void requestRobotList(OnRobotListUpdateListener listener) {
        mRequestRobotListListener = listener;
        if(mRequestRobotListListener != null) {
            mRequestRobotListListener.onRobotListUpdate(mRobots);
        }
    }

    @Override
    public void connectRobot(String baseUrl, OnRobotUpdateListener listener) {
        HttpEngine.setBaseUrl(baseUrl);
        StatusControler.g().connectRobot(getCurrentRobot(), listener);
    }

    @Override
    public Robot getCurrentRobot() {
        for (Robot robot : mRobots) {
            if(robot.baseURL.equals(HttpEngine.getBaseUrl())) {
                return robot;
            }
        }
        return mRobots.get(2);
    }

    @Override
    public void simpleMove(MotionControler.Speed speed, MotionControler.Direction direction, OnResultListener listener) {
        MotionControler.g().simpleMove(speed, direction, listener);
    }

    @Override
    public void stopMove(OnResultListener listener) {
        MotionControler.g().stop(listener);
    }

    @Override
    public void disconnectRobot() {
        StatusControler.g().disconnectRobot(null);
        HttpEngine.removeBaseUrl();
    }

    @Override
    public void requestRobotStatusUpdate(OnRobotStatusUpdateListener listener) {
        StatusControler.g().getRobotStatus(listener);
    }

    @Override
    public void requestMoveRobot(Point3D linear, Point3D angular, OnResultListener resultListener) {
        MotionControler.g().move(linear, angular, resultListener);
    }

    @Override
    public void requestRelocalization(Pose initPose, OnResultListener resultListener) {
        NavigationControler.g().relocalization(initPose, resultListener);
    }

    @Override
    public void setTarget(List<Pose> target, OnResultListener resultListener) {
        NavigationControler.g().setTarget(target, resultListener);
    }

    @Override
    public void cancelNavigation(OnResultListener resultListener) {
        NavigationControler.g().cancelNavigation(resultListener);
    }

    @Override
    public void requestMapList(OnMapListUpdateListener listener) {
        MapControler.g().getMapList(listener);
    }

    private RobotMap createMap(String assetName) throws IOException, JSONException {
        InputStream is = mContext.getAssets().open(assetName);
        RobotMap map = new RobotMap(is);
        is.close();
        return map;
    }

    @Override
    public void requestMap(String mapName, boolean isPng, boolean isUmap, boolean isLoad, boolean isArchive, OnMapUpdateListener listener) {
        MapControler.g().getMap(mapName, isPng, isUmap, isLoad, isArchive, listener);
    }

    @Override
    public void requestCurrentMap(boolean canEdit, OnMapUpdateListener listener) {
    }

    @Override
    public void exportMap(String mapName, OnExportMapListener listener) {
        MapControler.g().exportMap(mapName, listener);
    }

    @Override
    public void deleteMap(String mapName, OnResultListener listener) {
        MapControler.g().deleteMap(mapName, listener);
    }

    @Override
    public void importMap(RobotMap map, OnResultListener listener) {
//        MapControler.g().importMap(map.mMapName, map.mMapData, listener);
    }

    @Override
    public void updateMap(RobotMap map, OnResultListener listener) {
        MapControler.g().updateMap(map.mMapName, map.mMapData, listener);
    }

    @Override
    public void requestStartMapping(OnResultListener listener) {
        MapControler.g().startMapping(listener);
    }

    @Override
    public void requestUpdateMapping(OnResultListener listener) {
        MapControler.g().updateMapping(listener);
    }

    @Override
    public void requestStopMapping(String mapName, List<Line> wall, List<Line> tracker, OnResultListener listener) {
        MapControler.g().stopMapping(mapName, listener);
    }

    @Override
    public void cancelRelocalization(OnResultListener listener) {
        NavigationControler.g().cancelNavigation(listener);
    }

    @Override
    public void queryRelocalization(OnResultListener listener) {
        NavigationControler.g().queryRelocalization(listener);
    }

    @Override
    public void queryNavigationStatus(OnResultListener listener) {
        NavigationControler.g().queryNavigationStatus(listener);
    }
}
