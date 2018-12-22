package com.ubtrobot.uslam.control;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.ubtrobot.uslam.data.MapSaver;
import com.ubtrobot.uslam.net.HttpEngine;
import com.ubtrobot.uslam.net.bean.DeleteMapResponse;
import com.ubtrobot.uslam.net.bean.GetMapResponse;
import com.ubtrobot.uslam.net.bean.GetMappingStatusResponse;
import com.ubtrobot.uslam.net.bean.MapListResponse;
import com.ubtrobot.uslam.net.bean.RobotStatusResponse;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.utils.ImageUtils;
import com.ubtrobot.uslam.utils.Line;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.Result;
import com.ubtrobot.uslam.utils.RobotMap;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  地图相关的控制类
 *
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public class MapControler implements IControler {



    private static class MapControlerHolder {
        private static MapControler INSTANCE = new MapControler();
    }

    public static MapControler g() {
        return MapControlerHolder.INSTANCE;
    }

    private MapControler() {

    }

    public void getMapList(IRemoteRobotSdk.OnMapListUpdateListener listener) {
        HttpEngine.createUslamService().getMapList().enqueue(new Callback<MapListResponse>() {
            @Override
            public void onResponse(Call<MapListResponse> call, Response<MapListResponse> response) {
                MapListResponse body = response.body();
                List<RobotMap> list = new ArrayList<>();
                if (body != null) {
                    List<MapListResponse.NetBeanThumnailMapInfo> mapList = body.map_list;

                    for (MapListResponse.NetBeanThumnailMapInfo map : mapList) {
                        RobotMap robotMap = new RobotMap(map.map_name);
                        robotMap.mCreateTime = map.create_time;
                        robotMap.mMapSize = map.size;
                        robotMap.mAccessTime = map.access_time;
                        robotMap.mModifyTime = map.modify_time;
                        Bitmap thumbnail = ImageUtils.decodeBitmapFromBase64(map.thumbnail);
                        robotMap.mThumbnail = thumbnail;

                        list.add(robotMap);
                    }
                }
                listener.onMapListUpdate(list);
            }

            @Override
            public void onFailure(Call<MapListResponse> call, Throwable t) {

            }
        });
    }

    public void getMap(String mapName, boolean isPng, boolean isUmap, boolean isLoad, boolean isArchive, IRemoteRobotSdk.OnMapUpdateListener listener) {
        HttpEngine.createUslamService().getMap(mapName, isPng, isUmap, isLoad, isArchive).enqueue(new Callback<GetMapResponse>() {
            @Override
            public void onResponse(Call<GetMapResponse> call, Response<GetMapResponse> response) {

                if (response.isSuccessful()) {
                    GetMapResponse body = response.body();

                    RobotMap robotMap = MapSaver.parseRobotMapFromJson(body, isPng);
                    listener.onMapUpdate(robotMap);
                }
            }

            @Override
            public void onFailure(Call<GetMapResponse> call, Throwable t) {
                Log.i("okhttp", "on failure");
                listener.onMapUpdate(null);
            }
        });
    }

    public void exportMap(String mapName, boolean isArchive, IRemoteRobotSdk.OnExportMapListener listener) {
        HttpEngine.createUslamService().exportMap(mapName, false, true, true, isArchive).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String data = null;
                    try {
                        data = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!TextUtils.isEmpty(data)) {
                        if (listener != null) {
                            listener.onSuccess(data);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure();
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onFailure();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (listener != null) {
                    listener.onFailure();
                }
            }
        });
    }

    public void exportMap(String mapName, IRemoteRobotSdk.OnExportMapListener listener) {
        exportMap(mapName, true, listener);
    }

    public void updateMap(String mapName, String umap, IRemoteRobotSdk.OnResultListener listener) {

        JSONObject json = new JSONObject();
        try {
            json.put("map_name", mapName);
            json.put("umap", umap);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/json"), json.toString());

        HttpEngine.createUslamService().updateMap(mapName, requestBody).enqueue(new Callback<ResponseBody>() {
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

    public void deleteMap(String mapName, IRemoteRobotSdk.OnResultListener listener) {
        HttpEngine.createUslamService().deleteMap(mapName).enqueue(new Callback<DeleteMapResponse>() {
            @Override
            public void onResponse(Call<DeleteMapResponse> call, Response<DeleteMapResponse> response) {
                if (listener != null) {
                    listener.onResult(Result.makeResult(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Call<DeleteMapResponse> call, Throwable t) {
                if (listener != null) {
                    listener.onResult(Result.makeGloableErrResult(t.getMessage()));
                }
            }
        });
    }

    public void importMap(String mapName, String umap, IRemoteRobotSdk.OnResultListener listener) {

        JSONObject json = new JSONObject();
        try {
            json.put("map_name", mapName);
            json.put("umap", umap);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/json"), json.toString());

        HttpEngine.createUslamService().importMap(mapName, requestBody).enqueue(new Callback<ResponseBody>() {
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

    // TODO: 2018/12/16 后台接口未实现
    public void updateMapping(IRemoteRobotSdk.OnResultListener listener) {
        HttpEngine.createUslamService().updateMapping().enqueue(new Callback<ResponseBody>() {
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

    public void startMapping(IRemoteRobotSdk.OnResultListener listener) {
        HttpEngine.createUslamService().startMapping().enqueue(new Callback<ResponseBody>() {
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

    public void stopMapping(String mapName, IRemoteRobotSdk.OnResultListener listener) {
        HttpEngine.createUslamService().stopMapping(mapName).enqueue(new Callback<ResponseBody>() {
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

    public void getMappingState(IRemoteRobotSdk.OnResultListener listener) {
        HttpEngine.createUslamService().getMappingState().enqueue(new Callback<GetMappingStatusResponse>() {
            @Override
            public void onResponse(Call<GetMappingStatusResponse> call, Response<GetMappingStatusResponse> response) {
                if (listener != null) {
                    listener.onResult(Result.makeResult(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Call<GetMappingStatusResponse> call, Throwable t) {
                if (listener != null) {
                    listener.onResult(Result.makeGloableErrResult(t.getMessage()));
                }
            }
        });
    }

}