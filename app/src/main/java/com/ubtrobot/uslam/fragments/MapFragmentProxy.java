package com.ubtrobot.uslam.fragments;

import android.view.View;

import com.ubtrobot.uslam.utils.RobotStatus;

public interface MapFragmentProxy {

    void onCreateView(View rootView);

    void setMapFragment(MapFragment fragment);

    boolean shouldSwitchModeTo(MapFragment.Mode mode);

    boolean afterSwitchModeTo(MapFragment.Mode mode);

    void onUpdateRobotStatus(RobotStatus status);
}
