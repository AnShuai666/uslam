package com.ubtrobot.uslam.sdk;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.ubtrobot.uslam.control.MotionControler;
import com.ubtrobot.uslam.utils.Line;
import com.ubtrobot.uslam.utils.OnLineStatus;
import com.ubtrobot.uslam.utils.Point3D;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.Result;
import com.ubtrobot.uslam.utils.Robot;
import com.ubtrobot.uslam.utils.RobotMap;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakeLocalSdk implements IRemoteRobotSdk {
    private static final String TAG = "FakeLocalSdk";
    private final Handler mHandler = new Handler();
    private String mCurrentBaseUrl = null;
    private Robot mFakeRobot = generateRobot("1楼大堂",
            OnLineStatus.Online
            , "http://10.10.0.1:80/mFakeRobot",
            "map.umap");

    private Robot mBigMapRobot = generateRobot("大地图",
            OnLineStatus.Online
            , "http://10.10.0.11:80/mFakeRobot",
            "map_fsd7w6.umap");

    private Robot mNoMapRobot = generateRobot("2楼大堂\nno map",
            OnLineStatus.Online
            , "http://10.10.0.10:80/mFakeRobot",
            null);

    private Robot mOfflineRobot = generateRobot("3楼大堂",
            OnLineStatus.Offline,
            "http://10.10.0.2:80/mFakeRobot",
            "map.umap");
    private Robot mBusyRobot = generateRobot("5楼大堂",
            OnLineStatus.Busy,
            "http://10.10.0.3:80/mFakeRobot",
            "map2.umap");
    private List<Robot> mRobots;
    private Context mContext;

    @Override
    public void setContext(Context context) {
        mContext = context;
    }


    @Override
    public void setUUID(UUID accessToken) {

    }

    private Robot generateRobot(String name, OnLineStatus status, String baseUrl, String currentMap) {
        Robot robot =  new Robot();
        robot.name = name;
        robot.onLineStatus = status;
        robot.baseURL = baseUrl;
        robot.setCurrentMap(currentMap);
        return robot;
    }
    @Override
    public void requestRobotList(OnRobotListUpdateListener listener) {
        mRobots = new ArrayList<>();
        mRobots.add(mFakeRobot);
        mRobots.add(mBigMapRobot);
        mRobots.add(mNoMapRobot);
        mRobots.add(mOfflineRobot);
        mRobots.add(mBusyRobot);

        mHandler.postDelayed(() -> listener.onRobotListUpdate(mRobots), 500);
    }

    @Override
    public void connectRobot(String baseUrl, OnRobotUpdateListener listener) {
        mCurrentBaseUrl = baseUrl;
        mHandler.postDelayed(() -> listener.onRobotUpdate(getCurrentRobot()), 500);
    }

    @Override
    public Robot getCurrentRobot() {
        for (Robot robot : mRobots) {
            if(robot.baseURL.equals(mCurrentBaseUrl)) {
                return robot;
            }
        }
        return mRobots.get(0);
    }

    @Override
    public void simpleMove(MotionControler.Speed speed, MotionControler.Direction direction, OnResultListener listener) {

    }

    @Override
    public void stopMove(OnResultListener listener) {

    }

    @Override
    public void disconnectRobot() {
        mCurrentBaseUrl = null;
    }

    @Override
    public void requestRobotStatusUpdate(OnRobotStatusUpdateListener listener) {

    }

    @Override
    public void requestMoveRobot(Point3D linear, Point3D angular, OnResultListener resultListener) {

    }

    @Override
    public void requestRelocalization(Pose initPose, OnResultListener resultListener) {

    }

    @Override
    public void setTarget(List<Pose> target, OnResultListener resultListener) {

    }

    @Override
    public void cancelNavigation(OnResultListener resultListener) {

    }

    @Override
    public void requestMapList(OnMapListUpdateListener listener) {
        mHandler.postDelayed(() -> {
            List<RobotMap> maps = new ArrayList<>();
            maps.add(new RobotMap("map.umap"));
            maps.add(new RobotMap("map1.umap"));
            maps.add(new RobotMap("map2.umap"));
            listener.onMapListUpdate(maps);
        }, 500);
    }

    @Override
    public void requestMap(String mapName, boolean isPng, boolean isUmap, boolean isLoad, boolean isArchive, OnMapUpdateListener listener) {
        if(mContext != null) {
            InputStream is;
            try {
                is = mContext.getAssets().open(mapName);
                RobotMap map = new RobotMap(is);
                listener.onMapUpdate(map);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private RobotMap createMap(String assetName) throws IOException, JSONException {
        InputStream is = mContext.getAssets().open(assetName);
        RobotMap map = new RobotMap(is);
        is.close();
        return map;
    }

    @Override
    public void requestCurrentMap(boolean canEdit, OnMapUpdateListener listener) {
        mHandler.postDelayed(() -> {
            if (mContext != null) {
                Robot robot = getCurrentRobot();
                try {
                    InputStream is = mContext.getAssets().open(robot.getCurrentMapName());
                    RobotMap map = new RobotMap(is);
                    listener.onMapUpdate(map);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "can not get assert maps, context is null.");
            }
        }, 500);
    }

    @Override
    public void exportMap(String mapName, OnExportMapListener listener) {

    }

    @Override
    public void deleteMap(String mapName, OnResultListener listener) {

    }

    @Override
    public void importMap(RobotMap map, OnResultListener listener) {

    }

    @Override
    public void updateMap(RobotMap map, OnResultListener listener) {

    }

    @Override
    public void requestStartMapping(OnResultListener listener) {

    }

    @Override
    public void requestUpdateMapping(OnResultListener listener) {

    }

    @Override
    public void requestStopMapping(String mapName, List<Line> wall, List<Line> tracker, OnResultListener listener) {

    }

    @Override
    public void cancelRelocalization(OnResultListener listener) {

    }

    @Override
    public void queryRelocalization(OnResultListener listener) {

    }

    @Override
    public void queryNavigationStatus(OnResultListener listener) {

    }
}
