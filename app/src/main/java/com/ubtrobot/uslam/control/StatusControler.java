package com.ubtrobot.uslam.control;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ubtrobot.uslam.net.HttpEngine;
import com.ubtrobot.uslam.net.HttpRet;
import com.ubtrobot.uslam.net.bean.ConnectResponse;
import com.ubtrobot.uslam.net.bean.RobotStatusResponse;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.utils.Base64Utils;
import com.ubtrobot.uslam.utils.ImageUtils;
import com.ubtrobot.uslam.utils.Point;
import com.ubtrobot.uslam.utils.Point3D;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.Result;
import com.ubtrobot.uslam.utils.Robot;
import com.ubtrobot.uslam.utils.RobotMap;
import com.ubtrobot.uslam.utils.RobotStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 机器人状态的控制类
 *
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class StatusControler implements IControler {

    private static class StateControlerHolder {
        private static StatusControler INSTANCE = new StatusControler();
    }

    public static StatusControler g() {
        return StateControlerHolder.INSTANCE;
    }

    private StatusControler() {

    }

    public void connectRobot(Robot currentRobot, IRemoteRobotSdk.OnRobotUpdateListener listener) {
        HttpEngine.createUslamService().connectRobot().enqueue(new Callback<ConnectResponse>() {
            @Override
            public void onResponse(Call<ConnectResponse> call, Response<ConnectResponse> response) {
                int code = response.code();
                if (code == HttpRet.SUCCESS) {
                    listener.onRobotUpdate(currentRobot);
                } else if (code == HttpRet.ROBOT_BUSY) {

                }
            }

            @Override
            public void onFailure(Call<ConnectResponse> call, Throwable t) {

            }
        });
    }

    public void disconnectRobot(IRemoteRobotSdk.OnResultListener listener) {
        HttpEngine.createUslamService().disconnectRobot().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (listener != null) {
                    listener.onResult(Result.makeResult(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (listener != null) {
                    listener.onResult(Result.makeGloableErrResult(t.getMessage()));
                }
            }
        });
    }

    public void getRobotStatus(boolean lidar, boolean current_pose,
                               boolean path, boolean realtime_map,
                               boolean base64, boolean relocalization,
                               boolean navigation, boolean mapping,
                               boolean mode, boolean speed,
                               IRemoteRobotSdk.OnRobotStatusUpdateListener listener) {
        Map<String, Boolean> map = new HashMap<>();
        map.put("lidar", lidar);
        map.put("current_pose", current_pose);
        map.put("path", path);
        map.put("realtime_map", realtime_map);
        map.put("base64", base64);
        map.put("relocalization", relocalization);
        map.put("navigation", navigation);
        map.put("mapping", mapping);
        map.put("mode", mode);
        map.put("speed", speed);
        HttpEngine.createUslamService().getRobotState(map).enqueue(new Callback<RobotStatusResponse>() {
            @Override
            public void onResponse(Call<RobotStatusResponse> call, Response<RobotStatusResponse> response) {
                RobotStatusResponse body = response.body();
                if (body == null) return;
                RobotStatusResponse.NetBeanData data = body.data;
                if (data != null) {
                    RobotStatus robotStatus = new RobotStatus();

                    robotStatus.isRecalization = RobotStatusPaser.parseSubStatus(data.relocalization);
                    robotStatus.isNavigation =  RobotStatusPaser.parseSubStatus(data.navigation);
                    robotStatus.isMapping =  RobotStatusPaser.parseSubStatus(data.mapping);
                    Log.i("leo", "isReloac " + data.relocalization
                            + "\nisNav " + data.navigation
                            + "\nisMapping " + data.mapping);

                    // 设置Pose
                    RobotStatusResponse.NetBeanOrigin currentpose = data.current_pose;
                    if (currentpose != null) {
                        Pose pose = new Pose(currentpose.x, currentpose.y, currentpose.theta);
                        robotStatus.currentPose = pose;
                    }

                    // 设置lidar
                    List<Point> lidar = data.lidar;
                    if (lidar != null
                            && lidar.size() > 0) {
                        robotStatus.lidar = lidar;
                    }

                    // 设置实时地图
                    RobotStatusResponse.NetBeanMap realtime_map = data.realtime_map;
                    if (realtime_map != null) {
                        String map_data = realtime_map.map_data;
                        if (map_data != null) {
                            RobotMap map = new RobotMap();
                            map.setRealtimeMap(true);
                            Bitmap bitmap = ImageUtils.decodeBitmapFromBase64(map_data);
                            map.setBitmap(bitmap);
                            RobotStatusResponse.NetBeanMapInfo info = data.realtime_map.info;
                            map.setResolution(info.resolution);
                            map.setHeight(info.height);
                            map.setWidth(info.width);
                            Pose pose = new Pose(info.origin.x, info.origin.y, info.origin.theta);
                            map.setOrigin(pose);
                            robotStatus.map = map;
                        }
                    }

                    if (listener != null) {
                        listener.onRobotStatusUpdate(robotStatus);
                    }
                }
            }

            @Override
            public void onFailure(Call<RobotStatusResponse> call, Throwable t) {
                Log.e("okhttp", "net error!");
                t.printStackTrace();
            }
        });
    }

    public void getRobotStatus(IRemoteRobotSdk.OnRobotStatusUpdateListener listener) {
        getRobotStatus(true, true, true, true, true, true, true, true, true, true, listener);
    }

    public void getMappingRealTimeStatus(IRemoteRobotSdk.OnRobotStatusUpdateListener listener) {
        getRobotStatus(false, false, false, true, true, false, false, false, false, false, listener);
    }

    public void getMoveRealTimeStatus(IRemoteRobotSdk.OnRobotStatusUpdateListener listener) {
        getRobotStatus(true, true, false, false, false, false, false, false, false, true, listener);
    }
}
