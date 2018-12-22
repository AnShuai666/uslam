package com.ubtrobot.uslam.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Pose {

    public Point point = new Point();
    public float theta;
    static int sNameIndex = 0;
    public String poseName;

    public Pose(float x, float y, float theta) {
        set(x, y, theta);
    }

    public Pose(JSONArray jsonArray) throws JSONException {
        point.x = (float) jsonArray.getDouble(0);
        point.y = (float)jsonArray.getDouble(1);
        theta = (float)jsonArray.getDouble(2);
    }

    public Point getPoint() {
        return point;
    }

    public float getX() {
        return point.x;
    }

    public float getY() {
        return point.y;
    }

    public float getTheta() {
        return theta;
    }

    public float getThetaInDegree() {
        return (float) (theta * 180 / Math.PI);
    }

    public void setX(float x) {
        point.x = x;
    }

    public void setY(float y) {
        point.y = y;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }

    @Override
    public String toString() {
        return String.format("{x:%.2f, y:%.2f, theta:%.2f}", getX(), getY(), theta);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("x", getX());
            json.put("y", getY());
            json.put("theta", theta);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void set(float x, float y, float theta) {
        this.point.x = x;
        this.point.y = y;
        this.theta =theta;
    }

    public void generateName() {
        poseName = "#" + sNameIndex;
        sNameIndex++;
    }
}
