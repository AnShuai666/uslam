package com.ubtrobot.uslam.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class Point3D {
    public float x;
    public float y;
    public float z;

    @Override
    public String toString() {
        return String.format("{x: %.2f, y: %.2f, z: %.2f}", x, y, z);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("x", x);
            json.put("y", y);
            json.put("z", z);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

}
