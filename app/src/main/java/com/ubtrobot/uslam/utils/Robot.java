package com.ubtrobot.uslam.utils;

import android.text.TextUtils;
import android.util.Log;

import com.ubtrobot.uslam.control.RobotStatusPaser;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class Robot {

    public String name;

    public String baseURL;

    public RobotStatus status;

    public OnLineStatus onLineStatus;

    public int buttery;

    public RobotMode mode;

    public String currentMap;

    public Robot(Robot robot) {
        this.name = robot.name;
        this.baseURL = robot.baseURL;
        this.status = robot.status;
        this.onLineStatus = robot.onLineStatus;
        this.buttery = robot.buttery;
        this.mode = robot.mode;
    }

    public Robot() {

    }

    public Robot(JSONObject object) {
        set(object);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Robot) {
            Robot other = (Robot)obj;
            return other.baseURL.equals(baseURL);
        }
        if(obj instanceof JSONObject) {
            JSONObject json = (JSONObject)obj;
            try {
                return baseURL.equals(json.getString("base_url"));
            } catch (JSONException e) {
                return false;
            }
        }
        return false;
    }

    public void setCurrentMap(String currentMap) {
        this.currentMap = currentMap;
    }

    public String getCurrentMapName() {
        if (!TextUtils.isEmpty(currentMap)) {
            return currentMap.substring(currentMap.lastIndexOf("/") + 1, currentMap.length());
        } else {
            return "";
        }
    }

    public String getCurrentMapPath() {
        return currentMap;
    }
    private String getHostName(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return "Unknown";
        }
        String hostname = uri.getHost();
        // to provide faultproof result, check if not null then return only hostname, without www.
        if (hostname != null) {
            return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
        }
        return hostname;
    }
    public String getRobotName() {
        if (name == null || name.isEmpty()) {
            return getHostName(baseURL);
        }
        return name;
    }

    public void set(JSONObject object) {
        try {
            baseURL = object.getString("base_url");
            String strMode = object.getString("mode");
            if("navigation".equals(strMode)) {
                mode = RobotMode.Navigation;
            } else if("mapping".equals(strMode)) {
                mode = RobotMode.Mapping;
            } else {
                mode = RobotMode.Idle;
            }
            currentMap = object.getString("current_mapname");
            JSONObject online = object.getJSONObject("online");
            String status = online.getString("status");
            onLineStatus = RobotStatusPaser.parseOnLineStatus(status);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
