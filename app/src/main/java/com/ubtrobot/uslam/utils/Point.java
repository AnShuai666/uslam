package com.ubtrobot.uslam.utils;

import android.graphics.Matrix;

public class Point {
    public float x;
    public float y;

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point() {

    }

    public Point copy() {
        return new Point(x, y);
    }

    @Override
    public String toString() {
        return String.format("{x: %.2f, y:%.2f}", x, y);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public boolean simpleNear(Point point, float gap) {
        return (Math.abs(x - point.x) < gap) && (Math.abs(y - point.y) < gap);
    }

    public boolean near(Point point, float gap) {
        return (x - point.x) * (x - point.x) + (y - point.y) * (y - point.y) < gap * gap;
    }
}
