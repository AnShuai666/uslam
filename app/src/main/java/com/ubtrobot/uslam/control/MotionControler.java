package com.ubtrobot.uslam.control;

import com.ubtrobot.uslam.net.HttpEngine;
import com.ubtrobot.uslam.net.HttpRet;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.task.TaskExecutor;
import com.ubtrobot.uslam.utils.Point3D;
import com.ubtrobot.uslam.utils.Result;
import com.ubtrobot.uslam.utils.RobotStatus;

import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 机器人运动控制的控制类
 *
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class MotionControler implements IControler {

    public enum Speed {
        LOW,
        NORMAL,
        HIGH
    }

   public enum Direction {
        LEFT,
        RIGHT,
        FORWARD,
        BACKWARD,
        STOP
    }

    private static class RobotControlerHolder {
        private static MotionControler INSTANCE = new MotionControler();
    }

    public static MotionControler g() {
        return RobotControlerHolder.INSTANCE;
    }

    private MotionControler() {
    }

    private void move(boolean isSimple, boolean isStop, JSONObject json, IRemoteRobotSdk.OnResultListener listener) {

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/json"), json.toString());
        HttpEngine.createUslamService().controlRobot(isSimple, requestBody).enqueue(new Callback<ResponseBody>() {
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

    public MotionControler simpleMove(Speed speed, Direction direction, IRemoteRobotSdk.OnResultListener listener) {
        boolean isStop;
        JSONObject json = new JSONObject();
        try {
            json.put("speed", MotionParser.paserSpeed(speed));
            json.put("direction", MotionParser.parseDirection(direction));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (direction == Direction.STOP) {
            isStop = true;
        } else {
            isStop = false;
        }
        move(true, isStop, json, listener);
        return this;
    }

    public MotionControler move(Point3D linear, Point3D angular, IRemoteRobotSdk.OnResultListener listener) {
        JSONObject root = new JSONObject();

        try {
            root.put("linear", linear.toJson());
            root.put("angular", angular.toJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        move(false,false, root, listener);
        return this;
    }

    public void stop(IRemoteRobotSdk.OnResultListener listener) {
       simpleMove(Speed.LOW, Direction.STOP, listener);
    }


}
