package com.ubtrobot.uslam.net;

import com.ubtrobot.uslam.net.bean.ConnectResponse;
import com.ubtrobot.uslam.net.bean.DeleteMapResponse;
import com.ubtrobot.uslam.net.bean.ExportMapResponse;
import com.ubtrobot.uslam.net.bean.GetMapResponse;
import com.ubtrobot.uslam.net.bean.GetMappingStatusResponse;
import com.ubtrobot.uslam.net.bean.MapListResponse;
import com.ubtrobot.uslam.net.bean.QueryNavigationStatusResponse;
import com.ubtrobot.uslam.net.bean.QueryRelocalizationResponse;
import com.ubtrobot.uslam.net.bean.RobotStatusResponse;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * @author leo
 * @date 2018/12/4
 * @email ao.liu@ubtrobot.com
 */
public interface UslamService {

// TODO: 2018/12/6 有些接口在调用后，客户端需要用一直轮询机器人状态接口([GET] http://{{base_url}}/robot/status/?navigation=true)，在获知机器人

    /**
     * 2.1
     *
     * @return
     */
    @GET("robot/connect")
    Call<ConnectResponse> connectRobot();

    /**
     * 2.2
     *
     * @return
     */
    @DELETE("robot/connect")
    Call<ResponseBody> disconnectRobot();

    /**
     * 2.3
     * 验证通过
     * @return
     */
    @GET("robot/status")
    Call<RobotStatusResponse> getRobotState(@QueryMap Map<String, Boolean> map);

    /**
     * 2.4
     *
     * @param isSimple
     * @param requestBody
     * @return
     */
    @POST("robot/control")
    Call<ResponseBody> controlRobot(@Query("simple") boolean isSimple, @Body RequestBody requestBody);

    /**
     * 3.1
     *
     * @param requestBody
     * @return
     */
    @POST("robot/relocalization")
    Call<ResponseBody> relocalization(@Body RequestBody requestBody);

    /**
     *  3.2
     * @return
     */
    @DELETE("robot/relocalization")
    Call<ResponseBody> cancelRelocalization();

    /**
     *  3.3
     * @return
     */
    @GET("robot/relocalization")
    Call<QueryRelocalizationResponse> queryRelocalizationResponse();

    /**
     * 3.4
     *
     * @param requestBody
     * @return
     */
    @POST("robot/navigation")
    Call<ResponseBody> setTarget(@Body RequestBody requestBody);

    /**
     * 3.5
     *
     * @return
     */
    @DELETE("robot/navigation")
    Call<ResponseBody> cancelNavigation();

    /**
     *  3.6
     * @return
     */
    @GET("robot/navigation")
    Call<QueryNavigationStatusResponse> queryNavigationState();


    /**
     * 4.1
     *
     * @return
     */
    @GET("robot/map_list")
    Call<MapListResponse> getMapList();

    /**
     * 4.2
     *
     * @param mapName
     * @param isPng
     * @param isUmap
     * @return
     */
    @GET("robot/map")
    Call<GetMapResponse> getMap(@Query("map_name") String mapName, @Query("png_map") boolean isPng, @Query("umap") boolean isUmap, @Query("will_load") boolean isLoad, @Query("archive") boolean isArchive);

    /**
     * 4.3
     *
     * @param mapName
     * @param isPng
     * @param isUmap
     * @return
     */
    @GET("robot/map")
    Call<ResponseBody> exportMap(@Query("map_name") String mapName, @Query("png_map") boolean isPng, @Query("umap") boolean isUmap, @Query("will_load") boolean isLoad, @Query("archive") boolean isArchive);

    /**
     * 4.4
     *
     * @param mapName
     * @param requestBody
     * @return
     */
    @PUT("robot/map")
    Call<ResponseBody> updateMap(@Query("mapname") String mapName, @Body RequestBody requestBody);

    /**
     * 4.5
     *
     * @param mapName
     * @return
     */
    @DELETE("robot/map")
    Call<DeleteMapResponse> deleteMap(@Query("mapname") String mapName);

    /**
     * 4.6
     *
     * @param mapName
     * @param requestBody
     * @return
     */
    @POST("robot/map")
    Call<ResponseBody> importMap(@Query("mapname") String mapName, @Body RequestBody requestBody);

    /**
     * 4.7
     *
     * @return
     */
    @POST("robot/mapping")
    Call<ResponseBody> startMapping();

    /**
     * 4.8
     *
     * @return
     */
    @DELETE("robot/mapping")
    Call<ResponseBody> stopMapping(@Query("map_name") String mapName);

    /**
     * 4.9
     *
     * @return
     */
    @GET("robot/mapping")
    Call<GetMappingStatusResponse> getMappingState();

    /**
     *  4.10
     * @return
     */
    @PUT("robot/mapping")
    Call<ResponseBody> updateMapping();
}
