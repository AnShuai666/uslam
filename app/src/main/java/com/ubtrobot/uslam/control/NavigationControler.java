package com.ubtrobot.uslam.control;

import com.ubtrobot.uslam.net.HttpEngine;
import com.ubtrobot.uslam.net.HttpRet;
import com.ubtrobot.uslam.net.bean.QueryNavigationStatusResponse;
import com.ubtrobot.uslam.net.bean.QueryRelocalizationResponse;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 导航相关的控制类
 *
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class NavigationControler implements IControler {

    private static class NavigationControlerHolder {
        private static NavigationControler INSTANCE = new NavigationControler();
    }

    public static NavigationControler g() {
        return NavigationControlerHolder.INSTANCE;
    }

    private NavigationControler() {

    }

    public void relocalization(Pose pose, IRemoteRobotSdk.OnResultListener listener) {

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/json"), pose.toJson().toString());

        HttpEngine.createUslamService().relocalization(requestBody).enqueue(new Callback<ResponseBody>() {
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

    public void setTarget(List<Pose> poses, int loopCount, boolean endToStart, IRemoteRobotSdk.OnResultListener listener) {
        JSONArray jsonArray = new JSONArray();
        for (Pose pose : poses) {
            jsonArray.put(pose.toJson());
        }
        JSONObject json = new JSONObject();
        try {
            json.put("target_list", jsonArray);
            json.put("loop_count", loopCount);
            json.put("end_to_start", endToStart);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/json"), json.toString());
        HttpEngine.createUslamService().setTarget(requestBody).enqueue(new Callback<ResponseBody>() {
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

    public void setTarget(List<Pose> poses, IRemoteRobotSdk.OnResultListener listener) {
        setTarget(poses, 1, false, listener);
    }

    public void cancelNavigation(IRemoteRobotSdk.OnResultListener listener) {

        HttpEngine.createUslamService().cancelNavigation().enqueue(new Callback<ResponseBody>() {
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

    public void queryRelocalization(IRemoteRobotSdk.OnResultListener listener) {
        HttpEngine.createUslamService().queryRelocalizationResponse().enqueue(new Callback<QueryRelocalizationResponse>() {
            @Override
            public void onResponse(Call<QueryRelocalizationResponse> call, Response<QueryRelocalizationResponse> response) {
                if (listener != null) {
                    listener.onResult(Result.makeResult(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Call<QueryRelocalizationResponse> call, Throwable t) {
                if (listener != null) {
                    listener.onResult(Result.makeGloableErrResult(t.getMessage()));
                }
            }
        });
    }

    public void queryNavigationStatus(IRemoteRobotSdk.OnResultListener listener) {
        HttpEngine.createUslamService().queryNavigationState().enqueue(new Callback<QueryNavigationStatusResponse>() {
            @Override
            public void onResponse(Call<QueryNavigationStatusResponse> call, Response<QueryNavigationStatusResponse> response) {
                if (listener != null) {
                    listener.onResult(Result.makeResult(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Call<QueryNavigationStatusResponse> call, Throwable t) {
                if (listener != null) {
                    listener.onResult(Result.makeGloableErrResult(t.getMessage()));
                }
            }
        });
    }

}
