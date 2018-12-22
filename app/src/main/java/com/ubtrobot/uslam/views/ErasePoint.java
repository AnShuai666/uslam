package com.ubtrobot.uslam.views;

import com.ubtrobot.uslam.utils.Point;

public class ErasePoint extends Point{

    public enum EraseType {
        MagicBrush,
        UnknownArea,
        FreeArea,
        Nothing,
    }

    EraseType mType = EraseType.Nothing;
    float mSize;

    public ErasePoint(Point point, EraseType type, float size) {
        super(point);
        this.mType = type;
        this.mSize = size;
    }

    public ErasePoint(float x, float y, EraseType type, float size) {
        super(x, y);
        this.mType = type;
        this.mSize = size;
    }
}
