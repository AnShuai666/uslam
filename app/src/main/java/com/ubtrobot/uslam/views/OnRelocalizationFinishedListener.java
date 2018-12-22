package com.ubtrobot.uslam.views;

import com.ubtrobot.uslam.utils.Pose;

public interface OnRelocalizationFinishedListener {
    void onResult(Pose pose);
}
